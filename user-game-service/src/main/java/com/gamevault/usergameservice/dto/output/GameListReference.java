package com.gamevault.usergameservice.dto.output;

import java.util.UUID;

public record GameListReference(
        UUID listId,
        String listName,
        Boolean isPublic
) {}
