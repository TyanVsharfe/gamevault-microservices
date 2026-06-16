package com.gamevault.achievementservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.achievementservice.config.AchievementInitProperties;
import com.gamevault.achievementservice.db.model.*;
import com.gamevault.achievementservice.db.repository.*;
import com.gamevault.achievementservice.dto.input.init.AchievementDto;
import com.gamevault.achievementservice.enums.AchievementCategory;
import com.gamevault.achievementservice.service.UserGameProjectionRebuildService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Configuration
@EnableConfigurationProperties(AchievementInitProperties.class)
public class AchievementDataInitializer {

    private final AchievementRepository achievementRepository;
    private final UserGameProjectionRebuildService projectionRebuildService;
    private final UserAchievementRepository userAchievementRepository;
    private final WebClient authServiceWebClient;
    private final AchievementInitProperties properties;

    public AchievementDataInitializer(AchievementRepository achievementRepository,
                                      UserGameProjectionRebuildService projectionRebuildService,
                                      UserAchievementRepository userAchievementRepository,
                                      @Qualifier("authServiceWebClient") WebClient authWebClient,
                                      AchievementInitProperties properties) {
        this.achievementRepository = achievementRepository;
        this.projectionRebuildService = projectionRebuildService;
        this.userAchievementRepository = userAchievementRepository;
        this.authServiceWebClient = authWebClient;
        this.properties = properties;
    }

    @Bean
    public CommandLineRunner initAchievements() {
        return args -> {
            if (properties.isEnabled()) {
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
        List<Achievement> achievements = loadOrGetAchievements();

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
                .doOnError(e -> log.error("Failed to fetch page {}", page, e))
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Auth service returned an empty response"));
    }

    private void initializeUserAchievements(List<UUID> userUUIDs, List<Achievement> achievements) {
        if (userUUIDs.isEmpty()) {
            return;
        }

        List<UserAchievement> userAchievements = new ArrayList<>();

        for (UUID userId : userUUIDs) {
            Set<Long> assignedAchievementIds = userAchievementRepository.findByUserId(userId).stream()
                    .map(userAchievement -> userAchievement.getAchievement().getId())
                    .collect(Collectors.toSet());

            achievements.stream()
                    .filter(achievement -> !assignedAchievementIds.contains(achievement.getId()))
                    .map(achievement -> new UserAchievement(userId, achievement))
                    .forEach(userAchievements::add);
        }

        if (!userAchievements.isEmpty()) {
            userAchievementRepository.saveAll(userAchievements);
            log.info("Created {} missing user achievements for {} users", userAchievements.size(), userUUIDs.size());
        }

        for (UUID userId : userUUIDs) {
            projectionRebuildService.rebuildAndRecalculate(userId, properties.getBatchSize());
        }
    }

    private List<Achievement> loadAchievementsFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("achievements.json");
        List<AchievementDto> dtos = new ObjectMapper().readValue(resource.getInputStream(), new TypeReference<>() {});
        return dtos.stream().map(this::toAchievement).collect(Collectors.toList());
    }

    private List<Achievement> loadOrGetAchievements() throws IOException {
        if (achievementRepository.count() == 0) {
            List<Achievement> achievements = loadAchievementsFromJson();
            List<Achievement> savedAchievements = StreamSupport.stream(
                            achievementRepository.saveAll(achievements).spliterator(),
                            false
                    )
                    .toList();
            log.info("Loaded {} achievements from JSON", savedAchievements.size());
            return savedAchievements;
        }

        List<Achievement> achievements = StreamSupport.stream(achievementRepository.findAll().spliterator(), false)
                .toList();
        log.info("Using {} existing achievements", achievements.size());
        return achievements;
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
