package com.gamevault.steamimportservice.client;

import com.gamevault.dto.IgdbSteamMatchDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

@Component
public class IgdbClient {
    private final WebClient igdbServiceWebClient;

    public IgdbClient(@Qualifier("igdbServiceWebClient") WebClient igdbServiceWebClient) {
        this.igdbServiceWebClient = igdbServiceWebClient;
    }

    public List<IgdbSteamMatchDto> findBySteamAppIds(Collection<Long> steamAppIds) {
        if (steamAppIds == null || steamAppIds.isEmpty()) {
            return List.of();
        }

        return igdbServiceWebClient.post()
                .uri("/igdb/steam/matches/by-appids")
                .bodyValue(steamAppIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<IgdbSteamMatchDto>>() {})
                .timeout(Duration.ofSeconds(20))
                .blockOptional()
                .orElseGet(List::of);
    }
}
