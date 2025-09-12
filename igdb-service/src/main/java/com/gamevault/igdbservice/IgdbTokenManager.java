package com.gamevault.igdbservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamevault.igdbservice.dto.IgdbTokenResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Component
public class IgdbTokenManager {

    private final WebClient igdbServiceWebClient;

    @Value("${igdb.client.id}")
    private String client_id;
    @Value("${igdb.client.secret}")
    private String client_secret;
    private String access_token;
    private Integer expires_in;
    private String token_type;

    public IgdbTokenManager(WebClient igdbServiceWebClient) {
        this.igdbServiceWebClient = igdbServiceWebClient;
    }

    @Scheduled(fixedRate = 4500000)
    public void getAPIKey() throws JsonProcessingException {

        JsonNode response = igdbServiceWebClient.post()
                .uri("https://id.twitch.tv/oauth2/token")
                .body(BodyInserters.fromFormData("client_id", client_id)
                        .with("client_secret", client_secret)
                        .with("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(JsonNode.class).block();

        IgdbTokenResponse igdbTokenResponse = new ObjectMapper().treeToValue(response, IgdbTokenResponse.class);

        System.out.println("Access Token: " + igdbTokenResponse.getAccess_token());
        System.out.println("Expires In: " + igdbTokenResponse.getExpires_in());
        System.out.println("Token Type: " + igdbTokenResponse.getToken_type());

        this.access_token = igdbTokenResponse.getAccess_token();
        this.expires_in = igdbTokenResponse.getExpires_in();
        this.token_type = igdbTokenResponse.getToken_type();
    }
}
