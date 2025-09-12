package com.gamevault.achievementservice.db.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserGameCacheId {
    private UUID user;
    private Long game_id;
}
