package com.gamevault.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum IgdbGameType {
    MAIN_GAME(0),
    DLC(1),
    EXPANSION(2),
    BUNDLE(3),
    STANDALONE_EXPANSION(4),
    MOD(5),
    EPISODE(6),
    SEASON(7),
    REMAKE(8),
    REMASTER(9),
    EXPANDED_GAME(10),
    PORT(11),
    FORK(12),
    PACK(13),
    UPDATE(14);

    private final int number;
    private static final Map<Integer, IgdbGameType> BY_NUMBER;

    static {
        BY_NUMBER = Arrays.stream(values())
                .collect(Collectors
                        .toUnmodifiableMap(
                                IgdbGameType::getNumber,
                                Function.identity())
                );
    }

    IgdbGameType(int number) {
        this.number = number;
    }

    public static IgdbGameType fromNumber(Integer number) {
        IgdbGameType category = BY_NUMBER.get(number);
        if (category == null) {
            throw new IllegalArgumentException("Unknown IGDB category number: " + number);
        }
        return category;
    }
}
