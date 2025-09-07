package com.gamevault.achievementservice.dto.input;

import com.gamevault.achievementservice.db.model.SeriesPart;
import com.gamevault.achievementservice.enums.AchievementCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AchievementForm(
        @NotNull List<AchievementTranslationForm> translations,
        @NotNull @Enumerated(EnumType.STRING) AchievementCategory category,
        @NotNull int exp,
        int requiredCount,
        List<SeriesPart> requiredGameIds,
        String iconUrl
) {}
