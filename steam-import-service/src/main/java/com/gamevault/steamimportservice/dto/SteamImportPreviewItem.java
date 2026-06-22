package com.gamevault.steamimportservice.dto;

import com.gamevault.dto.db.UserGameBatchData;
import com.gamevault.steamimportservice.enums.SteamMatchStatus;

import java.util.Map;

public record SteamImportPreviewItem(
        Long steamAppId,
        String steamTitle,
        Long playtimeForever,
        String steamIconUrl,
        SteamMatchStatus status,
        IgdbMatchedGame matchedGame
) {
    public SteamImportPreviewItem withUserData(Map<Long, UserGameBatchData> userDataByIgdbId) {
        if (matchedGame == null) {
            return this;
        }

        return new SteamImportPreviewItem(
                steamAppId,
                steamTitle,
                playtimeForever,
                steamIconUrl,
                status,
                matchedGame.withUserData(userDataByIgdbId.get(matchedGame.igdbId()))
        );
    }
}
