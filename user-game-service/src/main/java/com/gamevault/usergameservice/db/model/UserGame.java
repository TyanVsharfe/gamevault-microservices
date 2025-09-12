package com.gamevault.usergameservice.db.model;

import com.gamevault.usergameservice.dto.input.update.UserGameUpdateForm;
import com.gamevault.enums.GameStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "user_games")
public class UserGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID user;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ElementCollection
    @CollectionTable(
            name = "user_game_notes",
            joinColumns = @JoinColumn(name = "user_game_id")
    )
    @Column(name = "note_id", columnDefinition = "VARCHAR(36)")
    private List<UUID> notes = new ArrayList<>();

    @Setter
    @Column(columnDefinition = "TEXT")
    private String review;

    @Setter
    private boolean isFullyCompleted;

    @Setter
    private GameStatus status;
    @Setter
    private Double userRating;
    @Setter
    @Column(length = 512)
    private String userCoverUrl;

    @Setter
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Setter
    @Column(name = "updated_at")
    private Instant updatedAt;

    public UserGame(UUID user, Game game) {
        this.user = user;
        this.userCoverUrl = game.getCoverUrl();
        this.status = GameStatus.None;
        this.isFullyCompleted = false;

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        this.createdAt = offsetDateTime.toInstant();
        this.game = game;
    }

    public void updateDto(UserGameUpdateForm dto) {
        if (dto.status() != null) this.status = dto.status();
        if (dto.userRating() != null) this.userRating = dto.userRating();
        if (dto.review() != null) this.review = dto.review();
        if (dto.isFullyCompleted() != null) this.isFullyCompleted = dto.isFullyCompleted();

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        this.updatedAt = offsetDateTime.toInstant();
    }
}
