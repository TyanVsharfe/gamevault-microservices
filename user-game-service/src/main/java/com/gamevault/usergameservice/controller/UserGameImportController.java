package com.gamevault.usergameservice.controller;

import com.gamevault.dto.ImportGamesRequest;
import com.gamevault.dto.ImportGamesResponse;
import com.gamevault.usergameservice.service.UserGameImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users/{userId}/games/import")
public class UserGameImportController {
    private final UserGameImportService userGameImportService;

    public UserGameImportController(UserGameImportService userGameImportService) {
        this.userGameImportService = userGameImportService;
    }

    @PostMapping
    public ResponseEntity<ImportGamesResponse> importGames(
            @PathVariable UUID userId,
            @RequestBody ImportGamesRequest request
    ) {
        return ResponseEntity.ok(userGameImportService.importGames(userId, request.igdbIds()));
    }
}
