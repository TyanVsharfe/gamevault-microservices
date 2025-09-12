package com.gamevault.achievementservice.dto.input.init;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AchievementDto(
        @Enumerated(EnumType.STRING) String category,
        @Nullable Integer requiredCount,
        @Nullable List<Set<Long>> requiredGameIds,
        @NotNull String iconUrl,
        int exp,
        @NotNull TranslationDto ru,
        @NotNull TranslationDto en
) {}
