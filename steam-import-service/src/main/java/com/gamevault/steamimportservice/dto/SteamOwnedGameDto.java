package com.gamevault.steamimportservice.dto;

public record SteamOwnedGameDto(
        Long steamAppId,
        String title,
        Long playtimeForever,
        String iconUrl
) {}
