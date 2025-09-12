package com.gamevault.authservice.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "oauth2_registered_client")
public class RegisteredClientEntity {
    @Id
    private String id;
    private String clientId;
    private Instant clientIdIssuedAt;
    private String clientSecret;
    private Instant clientSecretExpiresAt;
    private String clientName;
    @Column(columnDefinition = "TEXT")
    private String clientAuthenticationMethods;
    @Column(columnDefinition = "TEXT")
    private String authorizationGrantTypes;
    @Column(columnDefinition = "TEXT")
    private String redirectUris;
    @Column(columnDefinition = "TEXT")
    private String postLogoutRedirectUris;
    @Column(columnDefinition = "TEXT")
    private String scopes;
    @Column(columnDefinition = "TEXT")
    private String clientSettings;
    @Column(columnDefinition = "TEXT")
    private String tokenSettings;
}

