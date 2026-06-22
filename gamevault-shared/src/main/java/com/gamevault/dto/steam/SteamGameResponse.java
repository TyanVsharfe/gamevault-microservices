package com.gamevault.dto.steam;

import com.fasterxml.jackson.annotation.JsonProperty;import java.util.List;

public record SteamGameResponse(
        @JsonProperty("game_count")
        Long gameCount,
        List<SteamGame> games
) {}

