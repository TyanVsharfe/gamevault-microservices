package com.gamevault.usergameservice.controller;

import com.gamevault.usergameservice.db.model.UserGame;
import com.gamevault.usergameservice.dto.input.update.*;
import com.gamevault.usergameservice.dto.output.UserReviewsDTO;
import com.gamevault.usergameservice.service.UserGameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
                                        Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        return ResponseEntity.ok().body(userGameService.getByIgdbId(igdbId, user));
    }

    @GetMapping("/{igdb-id}/reviews")
    public ResponseEntity<List<UserReviewsDTO>> getUserReviews(@PathVariable("igdb-id") Long igdbId) {
        return ResponseEntity.ok().body(userGameService.getGameReviews(igdbId));
    }

    @GetMapping
    public ResponseEntity<Iterable<UserGame>> getAll(@RequestParam(value = "status", required = false) String status,
                                                     Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        return ResponseEntity.ok().body(userGameService.getAll(status, user));
    }

    @PostMapping("/{igdb-id}")
    public ResponseEntity<UserGame> add(@PathVariable("igdb-id") Long igdbId,
                                        Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        System.out.println("!!!! " + jwt + "!!!!!!!");
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
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
                                       Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        userGameService.delete(igdbId, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{igdb-id}")
    public ResponseEntity<UserGame> put(@PathVariable("igdb-id") Long igdbId,
                                        @Valid @RequestBody UserGameUpdateForm userGameUpdateForm,
                                        Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        UserGame updated = userGameService.update(igdbId, user, userGameUpdateForm);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{igdb-id}/status")
    public ResponseEntity<UserGame> updateStatus(@PathVariable("igdb-id") Long igdbId,
                                                 @Valid @RequestBody StatusUpdateForm dto,
                                                 Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        return ResponseEntity.ok(userGameService.updateStatus(igdbId, user, dto.status()));
    }

    @PatchMapping("/{igdb-id}/fully-completed")
    public ResponseEntity<UserGame> updateFullyCompleted(@PathVariable("igdb-id") Long igdbId,
                                                         @Valid @RequestBody FullyCompletedUpdateForm dto,
                                                         Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        return ResponseEntity.ok(userGameService.updateFullyCompleted(igdbId, user, dto.fullyCompleted()));
    }

    @PatchMapping("/{igdb-id}/rating")
    public ResponseEntity<UserGame> updateRating(@PathVariable("igdb-id") Long igdbId,
                                                 @Valid @RequestBody UserRatingUpdateForm dto,
                                                 Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        return ResponseEntity.ok(userGameService.updateRating(igdbId, user, dto.userRating()));
    }

    @PatchMapping("/{igdb-id}/review")
    public ResponseEntity<UserGame> updateReview(@PathVariable("igdb-id") Long igdbId,
                                                 @Valid @RequestBody ReviewUpdateForm dto,
                                                 Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        return ResponseEntity.ok(userGameService.updateReview(igdbId, user, dto.review()));
    }

    @GetMapping("/exists/{igdb-id}")
    public ResponseEntity<Void> isContains(@PathVariable("igdb-id") Long igdbId,
                                           Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID user = UUID.fromString(jwt.getClaim("user_id").toString());
        if (userGameService.isContains(igdbId, user)) {
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.noContent().build();
        }
    }
}
