package com.gamevault.usergameservice.db.model;

import com.gamevault.usergameservice.dto.GameForm;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "games")
public class Game {
    @Id
    @Column(name = "igdb_id")
    private Long igdbId;
    private String title;
    @Column(length = 512)
    private String coverUrl;
    @Lob
    private String description;

    public Game(GameForm gameForm) {
        this.igdbId = gameForm.igdbId();
        this.title = gameForm.title();
        this.coverUrl = gameForm.coverUrl();
        this.description = gameForm.description();
    }
}
