package com.gamevault.events.user;

import com.gamevault.enums.GameStatus;

import java.util.UUID;

public record UserGameEvent(
        UUID user_id,
        Long game_id,
        GameStatus status,
        EventType type
) {
    public enum EventType {
        USER_GAME_ADDED,
        USER_GAME_COMPLETED,
        USER_GAME_UPSERTED,
        USER_GAME_DELETED
    }
}
