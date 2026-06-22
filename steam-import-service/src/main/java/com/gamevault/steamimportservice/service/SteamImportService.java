package com.gamevault.steamimportservice.service;

import com.gamevault.dto.steam.SteamGame;
import com.gamevault.dto.steam.SteamOwnedGamesResponse;
import com.gamevault.steamimportservice.client.SteamWebApiClient;
import com.gamevault.steamimportservice.dto.SteamOwnedGameDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class SteamImportService {
    private static final int MAX_STEAM_GAMES_FOR_PREVIEW = 200;

    private final SteamWebApiClient steamWebApiClient;

    public SteamImportService(SteamWebApiClient steamWebApiClient) {
        this.steamWebApiClient = steamWebApiClient;
    }

    public List<SteamOwnedGameDto> loadOwnedGames(Long steamId) {
        SteamOwnedGamesResponse response = steamWebApiClient.getOwnedGames(steamId);

        if (response == null || response.response() == null || response.response().games() == null) {
            log.warn("Steam owned games response is empty for steamId={}", steamId);
            return List.of();
        }

        List<SteamOwnedGameDto> games = response.response().games().stream()
                .filter(game -> game.appid() != null)
                .filter(game -> game.name() != null && !game.name().isBlank())
                .sorted(Comparator.comparing(
                        SteamGame::playtimeForever,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(MAX_STEAM_GAMES_FOR_PREVIEW)
                .map(this::toOwnedGame)
                .toList();

        log.info("Loaded {} Steam games for steamId={}", games.size(), steamId);
        return games;
    }

    private SteamOwnedGameDto toOwnedGame(SteamGame game) {
        return new SteamOwnedGameDto(
                game.appid(),
                game.name(),
                game.playtimeForever(),
                buildIconUrl(game)
        );
    }

    private String buildIconUrl(SteamGame game) {
        if (game.imgIconUrl() == null || game.imgIconUrl().isBlank()) {
            return null;
        }

        return "https://media.steampowered.com/steamcommunity/public/images/apps/%d/%s.jpg"
                .formatted(game.appid(), game.imgIconUrl());
    }
}
