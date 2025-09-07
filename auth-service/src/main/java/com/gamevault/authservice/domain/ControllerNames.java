package com.gamevault.authservice.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerNames {
    private static final String USERS_URI_PART = "/users";
    private static final String REGISTRATION_URI_PART = "/registration";
    private static final String LOGIN_URI_PART = "/login";

    public String getRegistrationUrl() {
        return USERS_URI_PART + REGISTRATION_URI_PART;
    }

    public String getLoginUrl() {
        return USERS_URI_PART + LOGIN_URI_PART;
    }

    public String getIgdbPattern() {
        return "/igdb/**";
    }
}
