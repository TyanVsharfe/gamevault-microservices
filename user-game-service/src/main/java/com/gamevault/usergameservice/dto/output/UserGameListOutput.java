package com.gamevault.usergameservice.dto.output;

import com.gamevault.usergameservice.db.model.UserGameListItem;

import java.util.List;
import java.util.UUID;

public record UserGameListOutput(
        UUID uuid,
        String name,
        String authorUsername,
        String description,
        Boolean isPublic,
        List<UserGameListItem> items,
        Boolean isOwned
) {}
