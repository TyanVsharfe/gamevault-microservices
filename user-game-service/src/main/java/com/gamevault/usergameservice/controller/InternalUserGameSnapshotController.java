package com.gamevault.usergameservice.controller;

import com.gamevault.dto.usergame.UserGameSnapshotItem;
import com.gamevault.usergameservice.service.UserGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserGameSnapshotController {
    private final UserGameService userGameService;

    @GetMapping("/{userId}/games")
    public ResponseEntity<List<UserGameSnapshotItem>> getSnapshot(
            @PathVariable UUID userId,
            @PageableDefault(size = 1000) Pageable pageable
    ) {
        return ResponseEntity.ok(userGameService.getSnapshot(userId, pageable));
    }
}
