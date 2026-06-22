package com.gamevault.steamimportservice.service;

import com.gamevault.dto.IgdbSteamMatchDto;
import com.gamevault.dto.db.UserGameBatchData;
import com.gamevault.dto.igdb.IgdbGameDto;
import com.gamevault.steamimportservice.client.IgdbClient;
import com.gamevault.steamimportservice.client.UserGameClient;
import com.gamevault.steamimportservice.component.SteamIgdbMatchResolver;
import com.gamevault.steamimportservice.dto.IgdbMatchedGame;
import com.gamevault.steamimportservice.dto.SteamImportPreviewItem;
import com.gamevault.steamimportservice.dto.SteamOwnedGameDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SteamImportPreviewService {
    private final SteamImportService steamImportService;
    private final UserGameClient userGameClient;
    private final IgdbClient igdbClient;
    private final SteamIgdbMatchResolver gameMatcher;

    public SteamImportPreviewService(SteamImportService steamImportService, UserGameClient userGameClient,
                                     IgdbClient igdbClient, SteamIgdbMatchResolver gameMatcher) {
        this.steamImportService = steamImportService;
        this.userGameClient = userGameClient;
        this.igdbClient = igdbClient;
        this.gameMatcher = gameMatcher;
    }

    public List<SteamImportPreviewItem> preview(Long steamId, UUID userId) {
        List<SteamOwnedGameDto> steamGames = steamImportService.loadOwnedGames(steamId);

        if (steamGames.isEmpty()) {
            return List.of();
        }

        Set<Long> steamAppIds = steamGames.stream()
                .map(SteamOwnedGameDto::steamAppId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, IgdbGameDto> appIdMatches = igdbClient.findBySteamAppIds(steamAppIds).stream()
                .filter(match -> match.steamAppId() != null && match.game() != null)
                .collect(Collectors.toMap(
                        IgdbSteamMatchDto::steamAppId,
                        IgdbSteamMatchDto::game,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        List<SteamImportPreviewItem> previewWithoutUserData = steamGames.stream()
                .map(steamGame -> gameMatcher.resolve(
                        steamGame,
                        Optional.ofNullable(appIdMatches.get(steamGame.steamAppId()))
                ))
                .toList();

        Set<Long> matchedIgdbIds  = previewWithoutUserData.stream()
                .map(SteamImportPreviewItem::matchedGame)
                .filter(Objects::nonNull)
                .map(IgdbMatchedGame::igdbId)
                .collect(Collectors.toSet());

        Map<Long, UserGameBatchData> userDataByIgdbId = userGameClient.getUserGameBatchData(userId, matchedIgdbIds );

        return previewWithoutUserData.stream()
                .map(item -> item.withUserData(userDataByIgdbId))
                .toList();
    }
}