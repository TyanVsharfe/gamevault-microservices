package com.gamevault.usergameservice.controller;

import com.gamevault.usergameservice.db.model.UserGame;
import com.gamevault.usergameservice.dto.input.UserGamesFilterParams;
import com.gamevault.usergameservice.dto.input.update.*;
import com.gamevault.usergameservice.dto.output.UserReviewsDTO;
import com.gamevault.usergameservice.service.UserGameService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/games")
public class UserGameController {
    private final UserGameService userGameService;

    public UserGameController(UserGameService userGameService) {
        this.userGameService = userGameService;
    }

    @GetMapping("/{igdb-id}")
    public ResponseEntity<UserGame> get(@PathVariable("igdb-id") Long igdbId,
                                        @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok().body(userGameService.getByIgdbId(igdbId, user));
    }

    @GetMapping("/{igdb-id}/reviews")
    public ResponseEntity<List<UserReviewsDTO>> getUserReviews(@PathVariable("igdb-id") Long igdbId) {
        return ResponseEntity.ok().body(userGameService.getGameReviews(igdbId));
    }

    @GetMapping
    public ResponseEntity<Page<UserGame>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable,
            @ModelAttribute UserGamesFilterParams filterParams,
            @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok().body(userGameService.getAll(user, pageable, filterParams));
    }

    @PostMapping("/{igdb-id}")
    public ResponseEntity<UserGame> add(@PathVariable("igdb-id") Long igdbId,
                                        @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        UserGame created = userGameService.add(igdbId, user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{userGameId}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{igdb-id}")
    public ResponseEntity<Void> delete(@PathVariable("igdb-id") Long igdbId,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        userGameService.delete(igdbId, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{igdb-id}")
    public ResponseEntity<UserGame> put(@PathVariable("igdb-id") Long igdbId,
                                        @Valid @RequestBody UserGameUpdateForm userGameUpdateForm,
                                        @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        UserGame updated = userGameService.update(igdbId, user, userGameUpdateForm);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{igdb-id}/status")
    public ResponseEntity<UserGame> updateStatus(@PathVariable("igdb-id") Long igdbId,
                                                 @Valid @RequestBody StatusUpdateForm dto,
                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userGameService.updateStatus(igdbId, user, dto.status()));
    }

    @PatchMapping("/{igdb-id}/fully-completed")
    public ResponseEntity<UserGame> updateFullyCompleted(@PathVariable("igdb-id") Long igdbId,
                                                         @Valid @RequestBody FullyCompletedUpdateForm dto,
                                                         @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userGameService.updateFullyCompleted(igdbId, user, dto.fullyCompleted()));
    }

    @PatchMapping("/{igdb-id}/rating")
    public ResponseEntity<UserGame> updateRating(@PathVariable("igdb-id") Long igdbId,
                                                 @Valid @RequestBody UserRatingUpdateForm dto,
                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userGameService.updateRating(igdbId, user, dto.userRating()));
    }

    @PatchMapping("/{igdb-id}/review")
    public ResponseEntity<UserGame> updateReview(@PathVariable("igdb-id") Long igdbId,
                                                 @Valid @RequestBody ReviewUpdateForm dto,
                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID user = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userGameService.updateReview(igdbId, user, dto.review()));
    }
}
