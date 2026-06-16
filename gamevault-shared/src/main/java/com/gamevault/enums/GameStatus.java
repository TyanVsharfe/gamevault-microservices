package com.gamevault.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum GameStatus {
    COMPLETED("completed"),
    PLAYING("playing"),
    PLAYED("played"),
    PLANNED("planned"),
    ABANDONED("abandoned"),
    NONE("none");

    private final String slug;
    private static final Map<String, GameStatus> BY_SLUG;

    static {
        BY_SLUG = Arrays.stream(values())
                .collect(Collectors
                        .toUnmodifiableMap(
                                GameStatus::getSlug,
                                Function.identity())
                );
    }

    GameStatus(String slug) {
        this.slug = slug;
    }

    @JsonValue
    public String toJson() {
        return slug;
    }

    @JsonCreator
    public static GameStatus fromJson(String value) {
        GameStatus status = BY_SLUG.get(value.toLowerCase());
        if (status == null) {
            throw new IllegalArgumentException("Invalid game status: " + value);
        }
        return status;
    }
}