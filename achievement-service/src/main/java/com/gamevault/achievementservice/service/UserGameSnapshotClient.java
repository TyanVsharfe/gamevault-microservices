package com.gamevault.achievementservice.service;

import com.gamevault.dto.usergame.UserGameSnapshotItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class UserGameSnapshotClient {
    private final WebClient webClient;

    public UserGameSnapshotClient(@Qualifier("userGameServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<UserGameSnapshotItem> fetchUserGames(UUID userId, int size, int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users/{userId}/games")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(userId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserGameSnapshotItem>>() {})
                .timeout(Duration.ofSeconds(10))
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Empty user-game snapshot response"));
    }
}
