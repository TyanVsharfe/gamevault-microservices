package com.gamevault.achievementservice.dto.output;

public record AchievementGameDTO(
        long id,
        String name,
        String coverUrl,
        Integer releaseYear
) {}
