package com.gamevault.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum GameModesIGDB {
    SINGLE_PLAYER("single-player"),
    MULTIPLAYER("multiplayer"),
    CO_OPERATIVE("co-operative"),
    SPLIT_SCREEN("split-screen"),
    MMO("massively-multiplayer-online-mmo"),
    BATTLE_ROYALE("battle-royale");

    private final String slug;
    private static final Map<String, GameModesIGDB> BY_SLUG;

    static {
        BY_SLUG = Arrays.stream(values())
                .collect(Collectors
                        .toUnmodifiableMap(
                                GameModesIGDB::getSlug,
                                Function.identity())
                );
    }

    GameModesIGDB(String slug) {
        this.slug = slug;
    }

    @JsonValue
    public String toJson() {
        return slug;
    }

    @JsonCreator
    public static GameModesIGDB fromJson(String value) {
        GameModesIGDB gameMode = BY_SLUG.get(value.toLowerCase());
        if (gameMode == null) {
            throw new IllegalArgumentException("Unknown mode: " + value);
        }
        return gameMode;
    }
}