package com.gamevault.achievementservice.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.achievementservice.db.model.*;
import com.gamevault.achievementservice.db.repository.*;
import com.gamevault.achievementservice.dto.input.*;
import com.gamevault.achievementservice.dto.output.*;
import com.gamevault.achievementservice.enums.AchievementCategory;
import com.gamevault.enums.GameStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserGameCacheRepository userGameCacheRepository;
    private final AchievementMapper achievementMapper;
    private final IgdbGameClient igdbGameClient;

    public AchievementService(
            AchievementRepository achievementRepository,
            UserAchievementRepository userAchievementRepository,
            UserGameCacheRepository userGameCacheRepository,
            AchievementMapper achievementMapper,
            IgdbGameClient igdbGameClient
    ) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userGameCacheRepository = userGameCacheRepository;
        this.achievementMapper = achievementMapper;
        this.igdbGameClient = igdbGameClient;
    }

    public Iterable<Achievement> getAllAchievements() {
        return achievementRepository.findAll();
    }

    public Iterable<Achievement> getAchievementsByCategory(AchievementCategory category) {
        return achievementRepository.findByCategory(category);
    }

    public Iterable<UserAchievementDTO> getUserAchievements(UUID userId, String lang) {
        List<UserAchievement> userAchievements = userAchievementRepository.findUserAchievementsByUserId(userId);
        List<Achievement> achievements = userAchievements.stream()
                .map(UserAchievement::getAchievement)
                .toList();
        Set<Long> completedGameIds =
                userGameCacheRepository.findGameIdsByUserAndStatus(userId, GameStatus.COMPLETED);
        Map<Long, JsonNode> gamesById = igdbGameClient.getGamesByIds(achievementMapper.collectAllGameIds(achievements));

        return userAchievements.stream().map(a -> {
            List<AchievementTranslation> translations = a.getAchievement().getTranslations();
            Optional<AchievementTranslation> optionalTranslation = translations.stream()
                    .filter(tr -> tr.getLanguage().equals(lang))
                    .findFirst();

            AchievementTranslation tr = optionalTranslation.orElseGet(() ->
                    translations.stream()
                            .filter(t -> t.getLanguage().equals("en"))
                            .findFirst()
                            .orElse(translations.get(0))
            );

            AchievementDTO achievementDTO = achievementMapper.toDto(
                    a.getAchievement(),
                    tr,
                    completedGameIds,
                    gamesById
            );

            return new UserAchievementDTO(
                    a.getId(),
                    achievementDTO,
                    a.getCurrentProgress(),
                    a.getIsCompleted()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public Achievement createAchievement(AchievementForm achievementForm) {
        Achievement achievement;
        switch (achievementForm.category().name()) {
            case("TOTAL_GAMES_COMPLETED") ->
                    achievement = new CountAchievement(
                            achievementForm.category(), achievementForm.requiredCount(), achievementForm.iconUrl(), achievementForm.exp());
            case("SERIES_COMPLETED") ->
                    achievement = new SeriesAchievement(
                            achievementForm.category(), achievementForm.requiredGameIds(), achievementForm.iconUrl(), achievementForm.exp());
            default -> throw new IllegalArgumentException("Category not exist");
        }
        for (AchievementTranslationForm atf: achievementForm.translations()) {
            AchievementTranslation achievementTranslation = new AchievementTranslation(
                    atf.language(), atf.name(), atf.description(), achievement);
            achievement.getTranslations().add(achievementTranslation);
        }

        Achievement saved = achievementRepository.save(achievement);
        log.info("Achievement with Id={} successfully added", saved.getId());
        return saved;
    }

    @Transactional
    public void initializeUserAchievements(UUID userId) {
        List<Achievement> allAchievements = (List<Achievement>) achievementRepository.findAll();
        if (userAchievementRepository.countByUserId(userId) == 0) {
            log.info("No achievements found for user '{}'. Assigning default achievements.", userId);
            List<UserAchievement> userAchievements = allAchievements.stream()
                    .map(achievement -> new UserAchievement(userId, achievement))
                    .collect(Collectors.toList());
            userAchievementRepository.saveAll(userAchievements);
            log.info("Default achievements assigned to user '{}'.", userId);
        }
        else {
            log.info("User '{}' already has achievements assigned.", userId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAchievementProgress(UUID userId, Long achievementId, Integer progress) {
        Optional<UserAchievement> userAchievementOpt = userAchievementRepository.findByUserIdAndAchievementId(userId, achievementId);

        if (userAchievementOpt.isPresent()) {
            UserAchievement userAchievement = userAchievementOpt.get();
            int oldProgress = userAchievement.getCurrentProgress();
            userAchievement.setCurrentProgress(progress);
            Achievement achievement = userAchievement.getAchievement();

            if (achievement instanceof CountAchievement countAchievement) {
                if (progress >= countAchievement.getRequiredCount() && !userAchievement.getIsCompleted()) {
                    userAchievement.setIsCompleted(true);
                    userAchievement.setAchievedAt(Instant.now());
                }
            }
            if (achievement instanceof SeriesAchievement seriesAchievement) {
                if (progress >= seriesAchievement.getRequiredGameIds().size() && !userAchievement.getIsCompleted()) {
                    userAchievement.setIsCompleted(true);
                    userAchievement.setAchievedAt(Instant.now());
                }
            }

            log.info("Achievement with Id={} successfully updated: old - {}; new - {}", achievementId, oldProgress, progress);
            userAchievementRepository.save(userAchievement);
            return;
        }

        throw new RuntimeException("User achievement not found");
    }
}
