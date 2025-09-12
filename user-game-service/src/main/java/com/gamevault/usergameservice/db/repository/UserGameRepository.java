package com.gamevault.usergameservice.db.repository;

import com.gamevault.usergameservice.db.model.UserGame;
import com.gamevault.enums.GameStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserGameRepository extends CrudRepository<UserGame, Long> {
    boolean existsByGame_IgdbIdAndUser(Long IgdbId, UUID user);
    Optional<UserGame> findUserGameByGame_IgdbIdAndUser(Long IgdbId, UUID user);
    Iterable<UserGame> findGamesByStatusAndUser(GameStatus status, UUID user);
    Iterable<UserGame> findGamesByUser(UUID user);
    List<UserGame> findByGameIgdbIdAndReviewIsNotNull(Long IgdbId);
    int deleteUserGameByGame_IgdbIdAndUser(Long IgdbId, UUID user);
}
