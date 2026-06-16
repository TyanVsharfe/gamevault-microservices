package com.gamevault.achievementservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IgdbGameClient {
    private final WebClient webClient;

    public IgdbGameClient(
            WebClient.Builder webClientBuilder,
            @Value("${igdb.service.url}") String igdbServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(igdbServiceUrl).build();
    }

    public Map<Long, JsonNode> getGamesByIds(Set<Long> gameIds) {
        if (gameIds.isEmpty()) {
            return Map.of();
        }

        try {
            List<JsonNode> games = webClient.post()
                    .uri("/igdb/games/batch")
                    .bodyValue(gameIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<JsonNode>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (games == null) {
                return Map.of();
            }

            return games.stream().collect(Collectors.toMap(
                    game -> game.path("id").asLong(),
                    Function.identity(),
                    (first, ignored) -> first
            ));
        } catch (RuntimeException exception) {
            log.warn("Failed to load game summaries from igdb-service", exception);
            return Map.of();
        }
    }
}
