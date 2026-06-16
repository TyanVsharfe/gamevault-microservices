package com.gamevault.achievementservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.achievementservice.db.model.Achievement;
import com.gamevault.achievementservice.db.model.AchievementTranslation;
import com.gamevault.achievementservice.db.model.CountAchievement;
import com.gamevault.achievementservice.db.model.SeriesAchievement;
import com.gamevault.achievementservice.db.model.SeriesPart;
import com.gamevault.achievementservice.dto.output.AchievementDTO;
import com.gamevault.achievementservice.dto.output.AchievementGameDTO;
import com.gamevault.achievementservice.dto.output.SeriesPartDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AchievementMapper {

    public AchievementDTO toDto(
            Achievement achievement,
            AchievementTranslation translation,
            Set<Long> completedGameIds,
            Map<Long, JsonNode> gamesById
    ) {
        if (achievement instanceof CountAchievement count) {
            return new AchievementDTO(
                    count.getId(),
                    translation.getName(),
                    translation.getDescription(),
                    count.getCategory().name(),
                    count.getExperiencePoints(),
                    count.getRequiredCount(),
                    count.getIconUrl(),
                    null
            );
        }

        if (achievement instanceof SeriesAchievement series) {
            List<SeriesPartDTO> parts = series.getRequiredGameIds().stream()
                    .map(part -> toSeriesPartDto(part, completedGameIds, gamesById))
                    .toList();

            return new AchievementDTO(
                    series.getId(),
                    translation.getName(),
                    translation.getDescription(),
                    series.getCategory().name(),
                    series.getExperiencePoints(),
                    parts.size(),
                    series.getIconUrl(),
                    parts
            );
        }

        throw new IllegalStateException("Unknown achievement type: " + achievement.getClass().getName());
    }

    public Set<Long> collectAllGameIds(List<Achievement> achievements) {
        return achievements.stream()
                .filter(SeriesAchievement.class::isInstance)
                .map(SeriesAchievement.class::cast)
                .flatMap(achievement -> achievement.getRequiredGameIds().stream())
                .flatMap(part -> part.getGameIds().stream())
                .collect(Collectors.toSet());
    }

    private SeriesPartDTO toSeriesPartDto(
            SeriesPart part,
            Set<Long> completedGameIds,
            Map<Long, JsonNode> gamesById
    ) {
        List<AchievementGameDTO> games = part.getGameIds().stream()
                .map(gamesById::get)
                .filter(Objects::nonNull)
                .map(this::toGameDto)
                .sorted(Comparator.comparingLong(AchievementGameDTO::id))
                .toList();

        boolean completed = part.getGameIds().stream().anyMatch(completedGameIds::contains);
        return new SeriesPartDTO(part.getId(), new LinkedHashSet<>(games), completed);
    }

    private AchievementGameDTO toGameDto(JsonNode game) {
        return new AchievementGameDTO(
                game.path("id").asLong(),
                game.path("name").asText(),
                game.path("cover").path("url").asText(null),
                extractYear(game.path("first_release_date"))
        );
    }

    private Integer extractYear(JsonNode releaseDate) {
        long epochSeconds = releaseDate.asLong(0);
        if (epochSeconds <= 0) {
            return null;
        }
        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).getYear();
    }
}
