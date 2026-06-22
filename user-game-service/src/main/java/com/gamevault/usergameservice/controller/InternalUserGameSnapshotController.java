package com.gamevault.usergameservice.controller;

import com.gamevault.dto.db.UserGameBatchData;
import com.gamevault.dto.usergame.UserGameSnapshotItem;
import com.gamevault.usergameservice.service.UserGameService;
import com.gamevault.usergameservice.service.enriched.UserGameEnrichmentLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserGameSnapshotController {
    private final UserGameService userGameService;
    private final UserGameEnrichmentLoader userGameEnrichmentLoader;

    @GetMapping("/{userId}/games")
    public ResponseEntity<List<UserGameSnapshotItem>> getSnapshot(@PathVariable UUID userId,
                                                                  @PageableDefault(size = 1000) Pageable pageable) {
        return ResponseEntity.ok(userGameService.getSnapshot(userId, pageable));
    }

    @PostMapping("/{userId}/games/batch-data")
    public ResponseEntity<List<UserGameBatchData>> getBatchData(@PathVariable UUID userId,
                                                                @RequestBody Set<Long> igdbIds) {
        return ResponseEntity.ok(
                userGameEnrichmentLoader.loadUserGameDataBatch(userId, igdbIds)
                        .values()
                        .stream()
                        .toList()
        );
    }
}
