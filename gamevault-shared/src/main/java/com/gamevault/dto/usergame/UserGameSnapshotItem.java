package com.gamevault.dto.usergame;

import com.gamevault.enums.GameStatus;

import java.util.UUID;

public record UserGameSnapshotItem(
        UUID userId,
        Long gameId,
        GameStatus status
) {}
