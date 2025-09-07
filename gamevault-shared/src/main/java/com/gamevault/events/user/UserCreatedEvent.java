package com.gamevault.events.user;

import java.util.UUID;

public record UserCreatedEvent(
        UUID user_id
){}
