package com.gamevault.dto;

import com.gamevault.enums.ImportItemStatus;

public record ImportGameItemResult(
        Long igdbId,
        ImportItemStatus status,
        String errorCode,
        String message
) {}