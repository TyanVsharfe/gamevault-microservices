package com.gamevault.achievementservice.db.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_achievements")
@Setter
@Getter
@NoArgsConstructor
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column
    private Instant achievedAt;

    @Column(nullable = false)
    private int currentProgress;

    @Column(nullable = false)
    private Boolean isCompleted;

    public UserAchievement(UUID uuid, Achievement achievement) {
        this.userId = uuid;
        this.achievement = achievement;
        this.achievedAt = null;
        this.currentProgress = 0;
        this.isCompleted = false;
    }
}
