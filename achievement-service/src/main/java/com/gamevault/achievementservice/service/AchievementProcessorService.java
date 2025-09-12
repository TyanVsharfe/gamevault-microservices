package com.gamevault.achievementservice.service;

import com.gamevault.achievementservice.db.model.Achievement;
import com.gamevault.achievementservice.db.model.*;
import com.gamevault.achievementservice.db.repository.UserGameCacheRepository;
import com.gamevault.achievementservice.enums.AchievementCategory;
import com.gamevault.enums.GameStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AchievementProcessorService {
    private final UserGameCacheRepository userGameRepository;
    private final AchievementService achievementService;

    public AchievementProcessorService(UserGameCacheRepository userGameRepository, AchievementService achievementService) {
        this.userGameRepository = userGameRepository;
        this.achievementService = achievementService;
    }

    private void checkTotalGamesCompleted(UUID user) {
        long totalCompleted = userGameRepository.countUserGameCacheByStatusAndUser(GameStatus.Completed, user);

        Iterable<Achievement> totalGamesAchievements =
                achievementService.getAchievementsByCategory(AchievementCategory.TOTAL_GAMES_COMPLETED);

        for (Achievement achievement : totalGamesAchievements) {
            achievementService.updateAchievementProgress(user, achievement.getId(), (int) totalCompleted);
        }
    }

    private void checkSeriesAchievement(UUID user) {
        Set<Long> completedGameIds = userGameRepository.findCompletedGameIdsByUser(user);

        Iterable<Achievement> totalGamesAchievements =
                achievementService.getAchievementsByCategory(AchievementCategory.SERIES_COMPLETED);

        for (Achievement achievement : totalGamesAchievements) {
            if (achievement instanceof SeriesAchievement seriesAchievement) {
                List<SeriesPart> requiredGameIds = seriesAchievement.getRequiredGameIds();
                long seriesProgress = requiredGameIds.stream()
                        .filter(part -> part.getGameIds()
                                .stream().anyMatch(completedGameIds::contains)).count();
                achievementService.updateAchievementProgress(user, seriesAchievement.getId(), (int) seriesProgress);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAchievementCompletion(UUID userId) {
        checkTotalGamesCompleted(userId);
        checkSeriesAchievement(userId);
    }
}
