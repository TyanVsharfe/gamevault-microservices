package com.gamevault.authservice.config;

import com.gamevault.authservice.service.UserService;
import com.gamevault.authservice.domain.ControllerNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ControllerNames controllerNames;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(AuthenticationEntryPoint entryPoint, ControllerNames controllerNames, UserService userService, PasswordEncoder passwordEncoder, CorsConfigurationSource corsConfigurationSource) {
        this.controllerNames = controllerNames;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/oauth2/**", "/.well-known/**").permitAll()

                        .requestMatchers("/users/registration").permitAll()
                        .requestMatchers("/api/authenticate").permitAll()

                        .requestMatchers("/users/uuids").hasAuthority("SCOPE_ACHIEVEMENT_INIT")

                        .requestMatchers("/users/logout").hasAnyAuthority("SCOPE_USER", "SCOPE_ADMIN")
                        .requestMatchers("/refresh-token").hasAnyAuthority("SCOPE_USER", "SCOPE_ADMIN")
                        .requestMatchers("/users/profile").hasAnyAuthority("SCOPE_USER", "SCOPE_ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Для OAuth2 flow нужны сессии
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/oauth2/")) {
                                response.sendRedirect("http://localhost:3000/login");
                            } else {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.getWriter().write("{\"error\":\"Unauthorized\"}");
                            }
                        })
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("SCOPE_");
        converter.setAuthoritiesClaimName("scope");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
