package com.gamevault.usergameservice.service.filter;

import com.gamevault.enums.GameStatus;
import com.gamevault.enums.IgdbGameType;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

public class UserGameFilterSpecifications {

    public static UserGameFilterSpecification usernameEquals(UUID user) {
        return (ug, game) -> Optional.ofNullable(user)
                .map(ug.userId::eq)
                .orElse(null);
    }

    public static UserGameFilterSpecification statusEquals(GameStatus status) {
        return (ug, game) -> Optional.ofNullable(status)
                .map(ug.status::eq)
                .orElse(null);
    }

    public static UserGameFilterSpecification ratingBetween(Integer min, Integer max) {
        return (ug, game) -> {
            if (min == null && max == null) {
                return null;
            }

            BooleanExpression expression = null;

            if (min != null) {
                expression = ug.userRating.goe(min);
            }

            if (max != null) {
                expression = expression == null
                        ? ug.userRating.loe(max)
                        : expression.and(ug.userRating.loe(max));
            }

            return expression;
        };
    }

    public static UserGameFilterSpecification hasRating(Boolean hasRating) {
        return (ug, game) -> Optional.ofNullable(hasRating)
                .map(has -> has
                        ? ug.userRating.isNotNull()
                        : ug.userRating.isNull())
                .orElse(null);
    }

    public static UserGameFilterSpecification hasReview(Boolean hasReview) {
        return (ug, game) -> Optional.ofNullable(hasReview)
                .map(has -> has
                        ? ug.review.isNotNull().and(ug.review.ne(""))
                        : ug.review.isNull().or(ug.review.eq("")))
                .orElse(null);
    }

    public static UserGameFilterSpecification isFullyCompleted(Boolean isFullyCompleted) {
        return (ug, game) -> Optional.ofNullable(isFullyCompleted)
                .map(ug.isFullyCompleted::eq)
                .orElse(null);
    }

    public static UserGameFilterSpecification gameTypeEquals(IgdbGameType gameType) {
        return (ug, game) -> Optional.ofNullable(gameType)
                .map(ug.game.category::eq)
                .orElse(null);
    }

    public static UserGameFilterSpecification dlcOrExpansion(Boolean dlcOnly) {
        return (ug, game) -> {
            if (dlcOnly == null) return null;

            BooleanExpression dlcExpr =
                    game.category.in(IgdbGameType.DLC, IgdbGameType.EXPANSION);

            return dlcOnly
                    ? dlcExpr
                    : dlcExpr.not().or(game.category.isNull());
        };
    }

    public static UserGameFilterSpecification titleContains(String title) {
        return (ug, game) -> Optional.ofNullable(title)
                .map(game.title::containsIgnoreCase)
                .orElse(null);
    }

    public static UserGameFilterSpecification createdBetween(LocalDate startDate, LocalDate endDate) {
        return (ug, game) -> {
            BooleanExpression expr = null;

            if (startDate != null) {
                Instant start = Instant.from(startDate.atStartOfDay(ZoneId.systemDefault()));
                expr = ug.createdAt.goe(start);
            }

            if (endDate != null) {
                Instant end = Instant.from(endDate.atStartOfDay(ZoneId.systemDefault()));
                expr = expr != null ? expr.and(ug.createdAt.loe(end)) : ug.createdAt.loe(end);
            }

            return expr;
        };
    }
}

