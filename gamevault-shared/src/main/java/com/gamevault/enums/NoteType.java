package com.gamevault.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum NoteType {
    GENERAL("general"),
    GAMEPLAY("gameplay"),
    GUIDE("guide"),
    COMBAT("combat"),
    LORE("lore");

    private final String slug;
    private static final Map<String, NoteType> BY_SLUG;

    static {
        BY_SLUG = Arrays.stream(values())
                .collect(Collectors
                        .toUnmodifiableMap(
                                NoteType::getSlug,
                                Function.identity())
                );
    }

    NoteType(String slug) {
        this.slug = slug;
    }

    @JsonValue
    public String toJson() {
        return slug;
    }

    @JsonCreator
    public static NoteType fromJson(String value) {
        NoteType noteType = BY_SLUG.get(value.toLowerCase());
        if (noteType == null) {
            throw new IllegalArgumentException("Unknown note type: " + value);
        }
        return noteType;
    }
}
