package com.gamevault.authservice.dto;

import com.gamevault.authservice.db.model.User;
import com.gamevault.authservice.enums.Subscription;

import java.util.List;
import java.util.UUID;

public record UserInfo (
        UUID id,
        String username,
        String email,
        String avatarUrl,
        Subscription subscription,
        List<String> roles
){
    public UserInfo(User user){
        this (
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getSubscription(),
                user.getRoles()
        );
    }
}
