package com.gamevault.igdbservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.igdbservice.dto.IgdbTokenResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Component
@Slf4j
public class IgdbTokenManager {

    private final WebClient igdbServiceWebClient;

    @Value("${igdb.client.id}")
    private String client_id;
    @Value("${igdb.client.secret}")
    private String client_secret;
    private String access_token;
    private Integer expires_in;
    private String token_type;

    private final AtomicReference<CompletableFuture<Void>> pendingRefresh = new AtomicReference<>(null);

    public IgdbTokenManager(WebClient igdbServiceWebClient) {
        this.igdbServiceWebClient = igdbServiceWebClient;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing IGDB token on startup...");
        try {
            refreshToken();
        } catch (Exception e) {
            log.error("Failed to initialize IGDB token", e);
        }
    }

    public void getApiKey() throws JsonProcessingException {
        log.info("Refreshing IGDB access token...");

        JsonNode response = igdbServiceWebClient.post()
                .uri("https://id.twitch.tv/oauth2/token")
                .body(BodyInserters.fromFormData("client_id", client_id)
                        .with("client_secret", client_secret)
                        .with("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(JsonNode.class).block();

        IgdbTokenResponse igdbTokenResponse = new ObjectMapper().treeToValue(response, IgdbTokenResponse.class);

        this.access_token = igdbTokenResponse.getAccess_token();
        this.expires_in = igdbTokenResponse.getExpires_in();
        this.token_type = igdbTokenResponse.getToken_type();

        log.info("Access token refreshed. Expires in: {} sec", igdbTokenResponse.getExpires_in());
    }

    public void refreshToken() {
        CompletableFuture<Void> myFuture = new CompletableFuture<>();

        boolean isOwner = pendingRefresh.compareAndSet(null, myFuture);

        CompletableFuture<Void> activeFuture = isOwner
                ? myFuture
                : pendingRefresh.get();

        if (isOwner) {
            try {
                getApiKey();
                myFuture.complete(null);
            } catch (Exception e) {
                myFuture.completeExceptionally(e);
            } finally {
                pendingRefresh.compareAndSet(myFuture, null);
            }
        }

        try {
            activeFuture.join();
        } catch (CompletionException e) {
            throw new RuntimeException("Token refresh failed", e.getCause());
        }
    }
}
