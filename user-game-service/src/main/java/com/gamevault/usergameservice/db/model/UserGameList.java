package com.gamevault.usergameservice.db.model;

import com.gamevault.usergameservice.dto.input.UserGameListForm;
import com.gamevault.usergameservice.dto.input.update.UserGameListUpdateForm;
import com.gamevault.usergameservice.dto.output.UserGameListOutput;
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
@EqualsAndHashCode(callSuper = false)
@Table(name = "USER_GAME_LISTS")
public class UserGameList extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Setter
    @Column(nullable = false, length = 100)
    private String name;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Setter
    private String authorUsername;

    @OneToMany(mappedBy = "userGameList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    private List<UserGameListItem> items = new ArrayList<>();

    @Setter
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Setter
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Setter
    @Column(nullable = false)
    private boolean isPublic = false;

    public UserGameList(UUID author, String authorUsername, UserGameListForm listForm) {
        this.authorId = author;
        this.authorUsername = authorUsername;
        this.name = listForm.name();
        if (listForm.isPublic() != null) {
            this.isPublic = listForm.isPublic();
        }
        if (listForm.description() != null) {
            this.description = listForm.description();
        }

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        this.createdAt = offsetDateTime.toInstant();
    }

    public UserGameList(UUID author, String authorUsername, UserGameList original) {
        this.authorId = author;
        this.authorUsername = authorUsername;
        this.name = original.getName();
        this.isPublic = original.isPublic;
        if (original.description != null) {
            this.description = original.description;
        }
        this.items = new ArrayList<>();

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        this.createdAt = offsetDateTime.toInstant();
    }

    public UserGameList(UUID author, String authorUsername,  String name, List<UserGameListItem> games) {
        this.authorId = author;
        this.authorUsername = authorUsername;
        this.name = name;
        this.items = games;
        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        this.createdAt = offsetDateTime.toInstant();
    }

    public UserGameListOutput toOutput(UUID currentUser) {
        return new UserGameListOutput(
                this.uuid,
                this.name,
                this.authorUsername,
                this.description,
                this.isPublic,
                this.items,
                this.isOwnedBy(currentUser)
        );
    }

    public void update(UserGameListUpdateForm form) {
        if (form.name() != null) {
            this.name = form.name();
        }
        if (form.description() != null) {
            this.description = form.description();
        }
        if (form.isPublic() != null) {
            this.isPublic = form.isPublic();
        }
    }

    public void addGame(Game game, Integer order) {
        UserGameListItem item = new UserGameListItem(this, game, order);
        this.items.add(item);
        updateTimestamp();
    }

    public void removeGame(Game game) {
        this.items.removeIf(item -> item.getGame().equals(game));
        updateTimestamp();
    }

    public void updateTimestamp() {
        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        this.updatedAt = offsetDateTime.toInstant();
    }

    public boolean isOwnedBy(UUID user) {
        return this.authorId.equals(user);
    }
}
