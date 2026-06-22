package com.gamevault.usergameservice.dto.output.enriched;

import com.gamevault.enums.IgdbGameType;
import com.gamevault.usergameservice.db.model.Game;

public record GameDto(
        Long igdbId,
        String title,
        String coverUrl,
        IgdbGameType category
) {
    public static GameDto fromEntity(Game game) {
        return new GameDto(
                game.getIgdbId(),
                game.getTitle(),
                game.getCoverUrl(),
                game.getCategory()
        );
    }
}
