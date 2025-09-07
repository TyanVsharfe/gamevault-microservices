package com.gamevault.achievementservice.dto.output;

public record UserAchievementDTO (
        Long id,
        AchievementDTO achievement,
        int currentProgress,
        boolean isCompleted
){}
