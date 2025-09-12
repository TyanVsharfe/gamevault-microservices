package com.gamevault.achievementservice.db.repository;

import com.gamevault.achievementservice.db.model.UserGameCache;
import com.gamevault.achievementservice.db.model.UserGameCacheId;
import com.gamevault.enums.GameStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;
import java.util.UUID;

public interface UserGameCacheRepository extends CrudRepository<UserGameCache, UserGameCacheId> {
    UserGameCache findByUserAndGameId(UUID userId, Long gameId);
    long countUserGameCacheByStatusAndUser(GameStatus status, UUID user);
    @Query("SELECT ug.gameId FROM UserGameCache ug WHERE ug.user = :userId AND ug.status = com.gamevault.enums.GameStatus.Completed")
    Set<Long> findCompletedGameIdsByUser(UUID userId);
}
