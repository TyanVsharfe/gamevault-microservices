package com.gamevault.usergameservice.service.filter;

import com.gamevault.usergameservice.dto.input.UserGamesFilterParams;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserGameFilterFactory {
    public List<UserGameFilterSpecification> createFilters(UserGamesFilterParams params, UUID user) {
        return List.of(
                UserGameFilterSpecifications.usernameEquals(user),
                UserGameFilterSpecifications.statusEquals(params.getStatus()),
                UserGameFilterSpecifications.ratingBetween(params.getMinRating(), params.getMaxRating()),
                UserGameFilterSpecifications.hasReview(params.getHasReview()),
                UserGameFilterSpecifications.isFullyCompleted(params.getIsFullyCompleted()),
                UserGameFilterSpecifications.dlcOrExpansion(params.getDlcOnly()),
                UserGameFilterSpecifications.gameTypeEquals(params.getGameType()),
                UserGameFilterSpecifications.titleContains(params.getTitle()),
                UserGameFilterSpecifications.createdBetween(params.getCreatedAfter(), params.getCreatedBefore())
        );
    }
}
