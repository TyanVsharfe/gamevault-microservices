package com.gamevault.usergameservice.db.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "user_cache")
@NoArgsConstructor
@EqualsAndHashCode
public class UserCache {
    @Id
    private UUID id;
    @Setter
    private String username;
    @Setter
    private String avatarUrl;

    public UserCache(UUID id, String username, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
}
