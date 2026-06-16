package com.gamevault.usergameservice.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.gamevault.enums.GameModesIGDB;
import com.gamevault.enums.GameStatus;
import com.gamevault.usergameservice.dto.input.update.UserGameModeUpdateForm;
import com.gamevault.usergameservice.dto.input.update.UserGameUpdateForm;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "user_games")
public class UserGame extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne
    @JoinColumn(name = "parent_game_id")
    @JsonBackReference
    private UserGame parentGame;

    @OneToMany(mappedBy = "parentGame", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private final List<UserGame> dlcs = new ArrayList<>();

    @Setter
    @OneToMany(mappedBy = "userGame", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Note> notes = new ArrayList<>();

    @OneToMany(mappedBy = "userGame", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<UserGameMode> userModes = new ArrayList<>();

    @Setter
    @Column(columnDefinition = "TEXT")
    private String review;

    @Setter
    private boolean isFullyCompleted;

    @Setter
    private String platform;

    @Setter
    private GameStatus status = GameStatus.NONE;

    private Double userRating;

    @Column(nullable = false)
    private boolean isOverallRating = true;

    @Column(nullable = false)
    private boolean isOverallStatus = true;

    @Setter
    @Column(length = 512)
    private String customCoverUrl;

    public UserGame(UUID user, Game game) {
        this.userId = user;
        this.customCoverUrl = game.getCoverUrl();
        this.isFullyCompleted = false;
        this.game = game;
        initializeUserModesFromGame();
    }

    public UserGame(UUID user, Game game, UserGame parent) {
        this.userId = user;
        this.customCoverUrl = game.getCoverUrl();
        this.isFullyCompleted = false;
        this.game = game;
        initializeUserModesFromGame();
        this.parentGame = parent;
    }

    public void initializeUserModesFromGame() {
        if (this.game != null && this.game.getGameModes() != null) {
            for (GameModesIGDB gm : this.game.getGameModes()) {
                this.userModes.add(new UserGameMode(this, gm));
            }
        }
    }

    public void updateDto(UserGameUpdateForm dto) {
        if (dto.status() != null) {
            this.status = dto.status();
            this.isOverallStatus = true;
            setOverallModeStatus(this.status);
        }
        if (dto.resetUserRating() != null && dto.resetUserRating()) {
            this.userRating = null;
        }
        else if (dto.userRating() != null) {
            this.userRating = dto.userRating();
            this.isOverallRating = true;
            setOverallModeRating(dto.userRating());
            clearModeRating();
        }
        if (dto.platform() != null) {
            this.platform = dto.platform();
        }
        if (dto.review() != null) {
            this.review = dto.review();
        }
        if (dto.isFullyCompleted() != null) {
            this.isFullyCompleted = dto.isFullyCompleted();
        }
    }

    private void setOverallModeStatus(GameStatus status) {
        for (UserGameMode m : userModes) {
            if (m.getStatus() != null) {
                m.setStatus(status);
            }
        }
    }

    private void setOverallModeRating(Double userRating) {
        for (UserGameMode m : userModes) {
            m.setUserRating(userRating);
        }
    }

    private void clearModeRating() {
        for (UserGameMode m : userModes) {
            if (m.getUserRating() != null) {
                m.setUserRating(null);
            }
        }
    }

    public void updateMode(GameModesIGDB mode, UserGameModeUpdateForm dto) {
        if (dto.status() != null) {
            setModeStatus(mode, dto.status());
            this.isOverallStatus = false;
            this.status = GameStatus.NONE;
        }
        if (dto.userRating() != null) {
            setModeRating(mode, dto.userRating());
            this.isOverallRating = false;
        }
    }

    private void setModeStatus(GameModesIGDB mode, GameStatus status) {
        if (this.getUserModes().size() > 1) {
            for (UserGameMode m : userModes) {
                if (m.getMode() == mode) {
                    m.setStatus(status);
                    return;
                }
            }
        }
    }

    public void setModeRating(GameModesIGDB mode, Double rating) {
        for (UserGameMode m : userModes) {
            if (m.getMode() == mode) {
                m.setUserRating(rating);
                computeOverallRating();
                return;
            }
        }
    }

    private void computeOverallRating() {
        List<Double> ratings = new ArrayList<>();
        for (UserGameMode m : userModes) {
            if (m.getUserRating() != null) {
                ratings.add(m.getUserRating());
            }
        }
        if (ratings.isEmpty()) return;
        Double overallRating = ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        setOverallRating(overallRating);
    }

    public void setOverallRating(Double rating) {
        this.userRating = rating;
        this.isOverallRating = true;
    }
}
