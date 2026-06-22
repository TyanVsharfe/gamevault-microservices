package com.gamevault.steamimportservice.controller;

import com.gamevault.dto.ImportGamesResponse;
import com.gamevault.steamimportservice.client.UserGameClient;
import com.gamevault.steamimportservice.dto.SteamImportPreviewItem;
import com.gamevault.steamimportservice.service.SteamImportPreviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/steam-import")
public class SteamImportController {
    private final SteamImportPreviewService previewService;
    private final UserGameClient userGameClient;

    public SteamImportController(SteamImportPreviewService previewService, UserGameClient userGameClient) {
        this.previewService = previewService;
        this.userGameClient = userGameClient;
    }

    @GetMapping("/{steamId}/preview")
    public ResponseEntity<List<SteamImportPreviewItem>> preview(@PathVariable Long steamId,
                                                                @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(previewService.preview(steamId, userId));
    }

    @PostMapping("")
    public ResponseEntity<ImportGamesResponse> importSelectedGames(@RequestBody List<Long> selectedGames,
                                                                   @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        ImportGamesResponse result = userGameClient.importGames(userId, selectedGames);
        return ResponseEntity.ok(result);
    }
}
