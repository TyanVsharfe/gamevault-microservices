package com.gamevault.achievementservice.db.repository;

import com.gamevault.achievementservice.db.model.UserGameCache;
import com.gamevault.achievementservice.db.model.UserGameCacheId;
import com.gamevault.enums.GameStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

public interface UserGameCacheRepository extends CrudRepository<UserGameCache, UserGameCacheId> {
    long countByStatusAndUser(GameStatus status, UUID user);

    @Transactional
    int deleteByGameIdAndUser(Long gameId, UUID user);

    @Transactional
    int deleteByUser(UUID user);

    @Query("SELECT ug.gameId FROM UserGameCache ug WHERE ug.user = :userId AND ug.status = :status")
    Set<Long> findGameIdsByUserAndStatus(
            @Param("userId") UUID userId,
            @Param("status") GameStatus status
    );
}
