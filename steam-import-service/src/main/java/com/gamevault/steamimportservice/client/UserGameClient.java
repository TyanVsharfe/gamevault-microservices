package com.gamevault.steamimportservice.client;

import com.gamevault.dto.ImportGamesRequest;
import com.gamevault.dto.ImportGamesResponse;
import com.gamevault.dto.db.UserGameBatchData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserGameClient {
    private final WebClient userGameServiceWebClient;

    public UserGameClient(@Qualifier("userGameServiceWebClient") WebClient userGameServiceWebClient) {
        this.userGameServiceWebClient = userGameServiceWebClient;
    }

    public ImportGamesResponse importGames(UUID userId, List<Long> igdbIds) {
        return userGameServiceWebClient.post()
                .uri("/internal/users/{userId}/games/import", userId)
                .bodyValue(new ImportGamesRequest(igdbIds))
                .retrieve()
                .bodyToMono(ImportGamesResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    public Map<Long, UserGameBatchData> getUserGameBatchData(UUID userId, Set<Long> igdbIds) {
        if (igdbIds == null || igdbIds.isEmpty()) {
            return Map.of();
        }

        List<UserGameBatchData> response = userGameServiceWebClient.post()
                .uri("/internal/users/{userId}/games/batch-data", userId)
                .bodyValue(igdbIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserGameBatchData>>() {})
                .timeout(Duration.ofSeconds(10))
                .blockOptional()
                .orElseGet(List::of);

        return response.stream()
                .collect(Collectors.toMap(
                        UserGameBatchData::getIgdbId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }
}
