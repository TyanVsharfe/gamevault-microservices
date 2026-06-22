package com.gamevault.steamimportservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamevault.dto.db.UserGameBatchData;

public record IgdbMatchedGame(
        Long igdbId,
        String name,
        String coverUrl,
        Integer releaseYear,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        UserGameBatchData userData
) {
    public IgdbMatchedGame withUserData(UserGameBatchData userData) {
        return new IgdbMatchedGame(igdbId, name, coverUrl, releaseYear, userData);
    }
}
