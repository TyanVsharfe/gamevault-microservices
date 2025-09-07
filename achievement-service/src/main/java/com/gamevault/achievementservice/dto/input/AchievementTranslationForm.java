package com.gamevault.achievementservice.dto.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AchievementTranslationForm(
        @NotNull
        @Pattern(regexp = "^(ru|en)$", message = "Language must be either 'ru' or 'en'")String language,
        @NotNull String description,
        @NotNull String name
) {}
