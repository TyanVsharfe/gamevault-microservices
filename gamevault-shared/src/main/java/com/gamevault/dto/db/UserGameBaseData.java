package com.gamevault.dto.db;

import com.gamevault.enums.GameStatus;

import java.time.Instant;

public record UserGameBaseData(
        Long userGameId,
        GameStatus status,
        Double userRating,
        String review,
        boolean isFullyCompleted,
        boolean isOverallRating,
        boolean isOverallStatus,
        String userCoverUrl,
        Instant createdAt,
        Instant updatedAt,
        Long notesCount
) {}

