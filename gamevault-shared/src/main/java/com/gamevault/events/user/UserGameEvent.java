package com.gamevault.events.user;

import java.util.UUID;

public record UserGameEvent(
        UUID user_id,
        Long game_id,
        EventType type
) {
    public enum EventType { USER_GAME_COMPLETED, USER_GAME_DELETED }
}
