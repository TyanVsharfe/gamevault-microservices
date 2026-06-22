package com.gamevault.steamimportservice.client;

import com.gamevault.dto.steam.SteamOwnedGamesResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class SteamWebApiClient {
    private final WebClient steamWebClient;
    private final String apiKey;

    public SteamWebApiClient(@Qualifier("steamWebClient") WebClient steamWebClient,
                             @Value("${steam.api.key}") String apiKey) {
        this.steamWebClient = steamWebClient;
        this.apiKey = apiKey;
    }

    public SteamOwnedGamesResponse getOwnedGames(Long steamId) {
        return steamWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/IPlayerService/GetOwnedGames/v0001/")
                        .queryParam("key", apiKey)
                        .queryParam("steamid", steamId)
                        .queryParam("include_appinfo", 1)
                        .queryParam("include_played_free_games", 1)
                        .build())
                .retrieve()
                .bodyToMono(SteamOwnedGamesResponse.class)
                .block();
    }
}
