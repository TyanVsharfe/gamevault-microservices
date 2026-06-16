package com.gamevault.achievementservice.dto.output;

import java.util.Set;

public record SeriesPartDTO(
        long id,
        Set<AchievementGameDTO> games,
        boolean completed
) {}
