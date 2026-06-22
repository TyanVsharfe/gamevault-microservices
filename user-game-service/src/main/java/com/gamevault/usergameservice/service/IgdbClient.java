package com.gamevault.usergameservice.service;

import com.gamevault.dto.igdb.IgdbGameDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class IgdbClient {
    @Value("${igdb.service.url}")
    private String igdbServiceUrl;

    private final WebClient igdbServiceWebClient;

    public IgdbClient(WebClient userGameServiceWebClient) {
        this.igdbServiceWebClient = userGameServiceWebClient;
    }

    public List<IgdbGameDto> searchGames(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return igdbServiceWebClient.get()
                .uri(igdbServiceUrl + "/igdb/games?query={query}", query)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<IgdbGameDto>>() {})
                .blockOptional()
                .orElseGet(List::of);
    }

    public IgdbGameDto getGame(Long igdbId) {
        return igdbServiceWebClient.get()
                .uri(igdbServiceUrl + "/igdb/games/{igdbId}", igdbId)
                .retrieve()
                .bodyToMono(IgdbGameDto.class)
                .block();
    }
}
