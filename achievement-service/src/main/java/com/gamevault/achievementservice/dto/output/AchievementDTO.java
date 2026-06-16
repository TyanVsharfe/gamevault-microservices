package com.gamevault.achievementservice.dto.output;

import org.springframework.lang.Nullable;

import java.util.List;

public record AchievementDTO (
        Long id,
        String name,
        String description,
        String category,
        int experiencePoints,
        int requiredCount,
        String iconUrl,
        @Nullable List<SeriesPartDTO> seriesParts
){}
