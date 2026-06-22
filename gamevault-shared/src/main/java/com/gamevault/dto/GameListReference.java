package com.gamevault.dto;

import java.util.UUID;

public record GameListReference(
        UUID listId,
        String listName,
        Boolean isPublic
) {}
