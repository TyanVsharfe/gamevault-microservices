package com.gamevault.usergameservice.controller;

import com.gamevault.usergameservice.dto.output.enriched.EnrichedGameDto;
import com.gamevault.usergameservice.dto.output.enriched.EnrichedGameSearchDto;
import com.gamevault.usergameservice.service.enriched.EnrichedGameQueryService;
import com.gamevault.usergameservice.service.enriched.EnrichedGameService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {
    private final EnrichedGameService enrichedGameService;
    private final EnrichedGameQueryService enrichedGameQueryService;

    public GameController(EnrichedGameService enrichedGameService, EnrichedGameQueryService enrichedGameQueryService) {
        this.enrichedGameService = enrichedGameService;
        this.enrichedGameQueryService = enrichedGameQueryService;
    }

    @GetMapping("/search")
    public List<EnrichedGameSearchDto> search(
            @RequestParam String query,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return enrichedGameQueryService.searchGamesWithUserData(query, userId);
    }

    @GetMapping("/{igdb-id}")
    public ResponseEntity<EnrichedGameDto> get(
            @PathVariable("igdb-id") Long igdbId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return ResponseEntity.ofNullable(enrichedGameService.getGameWithUserData(igdbId, userId));
    }
}
