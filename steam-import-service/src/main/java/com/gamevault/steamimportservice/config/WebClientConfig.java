package com.gamevault.steamimportservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService
                );

        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }

    @Bean
    public WebClient userGameServiceWebClient(
            WebClient.Builder webClientBuilder,
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${user-game.service.url}") String userGameServiceUrl
    ) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        oauth2Client.setDefaultClientRegistrationId("steam-import-service");

        return webClientBuilder
                .baseUrl(userGameServiceUrl)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    @Bean
    public WebClient igdbServiceWebClient(
            WebClient.Builder builder,
            @Value("${igdb.service.url}") String igdbServiceUrl
    ) {
        return builder
                .baseUrl(igdbServiceUrl)
                .build();
    }

    @Bean
    public WebClient steamWebClient(
            WebClient.Builder builder,
            @Value("${steam.api.base-url:https://api.steampowered.com}") String steamBaseUrl
    ) {
        return builder
                .baseUrl(steamBaseUrl)
                .build();
    }
}
