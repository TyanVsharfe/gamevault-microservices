package com.gamevault.usergameservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient userGameServiceWebClient(WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .build();
    }
}
