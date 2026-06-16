package com.gamevault.achievementservice.db.model;

import com.gamevault.enums.GameStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "user_games")
@IdClass(UserGameCacheId.class)
public class UserGameCache {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id", nullable = false)
    private UUID user;

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    public UserGameCache(UUID user, Long gameId, GameStatus status) {
        this.user = user;
        this.gameId = gameId;
        this.status = status;
    }
}
