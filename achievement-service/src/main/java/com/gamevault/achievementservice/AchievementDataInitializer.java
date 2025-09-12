package com.gamevault.achievementservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.achievementservice.config.AchievementInitProperties;
import com.gamevault.achievementservice.db.model.*;
import com.gamevault.achievementservice.db.repository.*;
import com.gamevault.achievementservice.dto.input.init.AchievementDto;
import com.gamevault.achievementservice.enums.AchievementCategory;
import com.gamevault.achievementservice.service.AchievementProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableConfigurationProperties(AchievementInitProperties.class)
public class AchievementDataInitializer {

    private final AchievementRepository achievementRepository;
    private final AchievementProcessorService achievementProcessorService;
    private final UserAchievementRepository userAchievementRepository;
    private final WebClient authServiceWebClient;
    private final AchievementInitProperties properties;

    public AchievementDataInitializer(AchievementRepository achievementRepository,
                                      AchievementProcessorService achievementProcessorService,
                                      UserAchievementRepository userAchievementRepository,
                                      WebClient authWebClient, AchievementInitProperties properties) {
        this.achievementRepository = achievementRepository;
        this.achievementProcessorService = achievementProcessorService;
        this.userAchievementRepository = userAchievementRepository;
        this.authServiceWebClient = authWebClient;
        this.properties = properties;
    }

    @Bean
    public CommandLineRunner initAchievements() {
        return args -> {
            if (properties.isEnabled() && achievementRepository.count() == 0) {
                try {
                    initializeAchievements();
                } catch (Exception e) {
                    log.error("Failed to initialize achievements", e);
                }
            }
        };
    }

    private void initializeAchievements() throws IOException {
        log.info("Starting achievement initialization");
        List<Achievement> achievements = loadAchievementsFromJson();
        achievementRepository.saveAll(achievements);
        log.info("Loaded {} achievements from JSON", achievements.size());

        int page = 0;
        List<UUID> userUUIDs;
        int totalUsers = 0;

        do {
            log.info("Fetching user UUIDs for page {}", page);
            userUUIDs = fetchUserUUIDs(page, properties.getBatchSize());

            initializeUserAchievements(userUUIDs, achievements);
            totalUsers += userUUIDs.size();
            page++;

        } while (!userUUIDs.isEmpty() && userUUIDs.size() == properties.getBatchSize());

        log.info("Achievement initialization completed successfully. Total users processed: {}", totalUsers);
    }

    private List<UUID> fetchUserUUIDs(int page, int size) {
        try {
            return authServiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(properties.getUsersEndpoint())
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Failed to fetch UUIDs. Status: {}", response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "Failed to fetch user UUIDs: " + response.statusCode() + " - " + errorBody)));
                    })
                    .bodyToMono(new ParameterizedTypeReference<List<UUID>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .doOnSuccess(uuids -> log.info("Fetched {} UUIDs for page {}", uuids.size(), page))
                    .doOnError(e -> log.error("Failed to fetch page {}: {}", page, e.getMessage()))
                    .onErrorReturn(Collections.emptyList())
                    .block();

        } catch (Exception e) {
            log.error("Error fetching user UUIDs for page {}", page, e);
            return Collections.emptyList();
        }
    }

    private void initializeUserAchievements(List<UUID> userUUIDs, List<Achievement> achievements) {
        if (userUUIDs.isEmpty()) {
            return;
        }

        List<UserAchievement> userAchievements = new ArrayList<>();

        for (UUID userId : userUUIDs) {
            if (userAchievementRepository.countByUserId(userId) == 0) {
                achievements.forEach(achievement ->
                        userAchievements.add(new UserAchievement(userId, achievement))
                );
            }
        }

        if (!userAchievements.isEmpty()) {
            userAchievementRepository.saveAll(userAchievements);
            log.info("Initialized achievements for {} users", userUUIDs.size());
        }

        userUUIDs.forEach(userId ->
                CompletableFuture.runAsync(() ->
                        achievementProcessorService.processAchievementCompletion(userId)
                )
        );
    }

    private List<Achievement> loadAchievementsFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("achievements.json");
        List<AchievementDto> dtos = new ObjectMapper().readValue(resource.getInputStream(), new TypeReference<>() {});
        return dtos.stream().map(this::toAchievement).collect(Collectors.toList());
    }

    private Achievement toAchievement(AchievementDto dto) {
        Achievement achievement;
        switch (dto.category()) {
            case("TOTAL_GAMES_COMPLETED") -> {
                assert dto.requiredCount() != null;
                achievement = new CountAchievement(AchievementCategory.valueOf(dto.category()), dto.requiredCount(), dto.iconUrl(), dto.exp());
            }
            case("SERIES_COMPLETED") -> {
                assert dto.requiredGameIds() != null;
                List<SeriesPart> seriesParts = new ArrayList<>(dto.requiredGameIds().size());
                for (Set<Long> part: Objects.requireNonNull(dto.requiredGameIds())) {
                    seriesParts.add(new SeriesPart(part));
                }
                achievement = new SeriesAchievement(AchievementCategory.valueOf(dto.category()), seriesParts, dto.iconUrl(), dto.exp());
            }
            default -> throw new IllegalArgumentException("Category not exist");
        }
        AchievementTranslation ruTranslation = new AchievementTranslation("ru", dto.ru().name(), dto.ru().description(), achievement);
        AchievementTranslation enTranslation = new AchievementTranslation("en", dto.en().name(), dto.en().description(), achievement);
        achievement.getTranslations().add(ruTranslation);
        achievement.getTranslations().add(enTranslation);
        return achievement;
    }
}
