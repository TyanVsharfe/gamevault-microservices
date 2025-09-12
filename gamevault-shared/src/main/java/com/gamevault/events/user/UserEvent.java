package com.gamevault.events.user;

import java.util.UUID;

public record UserEvent(
        UUID user_id,
        String username,
        String avatar_url,
        EventType type
) {
    public enum EventType { USER_CREATED, USER_UPDATED, USER_DELETED }
}
