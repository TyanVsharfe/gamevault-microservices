package com.gamevault.igdbservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient igdbServiceWebClient(WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .build();
    }
}
