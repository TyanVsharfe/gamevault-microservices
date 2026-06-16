package com.gamevault.usergameservice.db.repository;

import com.gamevault.dto.usergame.UserGameSnapshotItem;
import com.gamevault.usergameservice.db.model.UserGame;
import com.gamevault.enums.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserGameRepository extends CrudRepository<UserGame, Long> {
    @Query("""
        select new com.gamevault.dto.usergame.UserGameSnapshotItem(
            ug.userId,
            ug.game.igdbId,
            ug.status
        )
        from UserGame ug
        where ug.userId = :userId
        order by ug.id
        """)
    List<UserGameSnapshotItem> findSnapshotByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );
    Optional<UserGame> findUserGameByGame_IgdbIdAndUserId(Long IgdbId, UUID user);
    Page<UserGame> findGamesByStatusAndUserId(GameStatus status, UUID user, Pageable pageable);
    Page<UserGame> findGamesByUserId(UUID user, Pageable pageable);
    List<UserGame> findByGameIgdbIdAndReviewIsNotNull(Long IgdbId);
    int deleteUserGameByGame_IgdbIdAndUserId(Long IgdbId, UUID user);
}
