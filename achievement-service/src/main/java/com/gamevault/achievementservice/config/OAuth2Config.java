package com.gamevault.achievementservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OAuth2Config {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
    }

    @Bean
    public WebClient authServiceWebClient(WebClient.Builder webClientBuilder,
                                          OAuth2AuthorizedClientManager authorizedClientManager,
                                          @Value("${auth.service.url}") String authServiseUrl) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        oauth2Client.setDefaultClientRegistrationId("achievement-service");

        return webClientBuilder
                .baseUrl(authServiseUrl)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }
}
