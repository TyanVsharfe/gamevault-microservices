package com.gamevault.usergameservice.dto.output;

import com.gamevault.usergameservice.db.model.UserGame;

public record UserReviewsDTO(
        Long id,
        String username,
        String review,
        Double userRating
) {
    public UserReviewsDTO(UserGame userGame, String username) {
        this (
                userGame.getId(),
                username,
                userGame.getReview(),
                userGame.getUserRating()
        );
    }
}
