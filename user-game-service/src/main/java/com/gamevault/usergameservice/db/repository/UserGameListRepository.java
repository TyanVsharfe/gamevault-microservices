package com.gamevault.usergameservice.db.repository;

import com.gamevault.usergameservice.db.model.UserGameList;
import com.gamevault.dto.GameListReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGameListRepository extends CrudRepository<UserGameList, UUID> {
    Page<UserGameList> findByAuthorId(UUID user, Pageable pageable);
    List<UserGameList> findByIsPublicTrueAndAuthorId(UUID user);
    @Query("""
           SELECT GameListReference(
               ugl.uuid,
               ugl.name,
               ugl.isPublic
           )
           FROM UserGameListItem ugli
           JOIN ugli.userGameList ugl
           WHERE ugli.game.igdbId = :userGameId
           AND ugl.authorId = :user
           """)
    List<GameListReference> findListsByUserGameIdAndAuthorId(Long userGameId, UUID user);

    @EntityGraph(attributePaths = {"items", "items.game"})
    @Query("SELECT ugl FROM UserGameList ugl WHERE ugl.uuid = :listId")
    Optional<UserGameList> findByIdWithItems(UUID listId);

    boolean existsByUuidAndAuthorId(UUID listId, UUID user);
}
