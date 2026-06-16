package com.gamevault.usergameservice.dto.output;

import com.gamevault.enums.GameModesIGDB;
import com.gamevault.enums.GameStatus;
import com.gamevault.usergameservice.db.model.UserGameMode;

public record UserModeDto(
        Long id,
        GameModesIGDB modeName,
        GameStatus status,
        Double rating
) {
    public static UserModeDto fromUserGameMode(UserGameMode mode) {
        if (mode == null) return null;
        return new UserModeDto(
                mode.getId(),
                mode.getMode(),
                mode.getStatus(),
                mode.getUserRating()
        );
    }
}
