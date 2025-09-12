package com.gamevault.usergameservice.service;

import com.gamevault.events.user.UserGameEvent;
import com.gamevault.usergameservice.db.model.Game;
import com.gamevault.usergameservice.db.model.UserCache;
import com.gamevault.usergameservice.db.model.UserGame;
import com.gamevault.usergameservice.db.repository.GameRepository;
import com.gamevault.usergameservice.db.repository.UserCacheRepository;
import com.gamevault.usergameservice.db.repository.UserGameRepository;
import com.gamevault.usergameservice.dto.input.update.UserGameUpdateForm;
import com.gamevault.usergameservice.dto.output.UserReviewsDTO;
import com.gamevault.enums.GameStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserGameService {
    private final UserGameRepository userGameRepository;
    private final GameRepository gameRepository;
    private final UserCacheRepository userCacheRepository;
    private final GameService gameService;
    private final UserGameEventProducer userGameEventProducer;

    public UserGameService(UserGameRepository userGameRepository, GameRepository gameRepository,
                           UserCacheRepository userCacheRepository, GameService gameService,
                           UserGameEventProducer userGameEventProducer) {
        this.userGameRepository = userGameRepository;
        this.gameRepository = gameRepository;
        this.userCacheRepository = userCacheRepository;
        this.gameService = gameService;
        this.userGameEventProducer = userGameEventProducer;
    }

    public Iterable<UserGame> getAll(String status, UUID user) {
        if (status == null || status.isEmpty()) {
            return userGameRepository.findGamesByUser(user);
        }
        else {
            try {
                GameStatus statusEnum = GameStatus.valueOf(status);
                return userGameRepository.findGamesByStatusAndUser(statusEnum, user);
            }
            catch (IllegalArgumentException e) {
                throw new EntityNotFoundException("Invalid game status " + e.getMessage());
            }
        }
    }

    public List<UserReviewsDTO> getGameReviews(Long igdbId) {
        List<UserGame> reviews = userGameRepository.findByGameIgdbIdAndReviewIsNotNull(igdbId);
        return reviews.stream()
                .filter(review -> !review.getReview().isEmpty())
                .map(r -> {
                    String username = userCacheRepository.findById(r.getUser()).map(UserCache::getUsername).orElse("Anonymous");
                    return new UserReviewsDTO(r, username);
                })
                .toList();
    }

    public UserGame getByIgdbId(Long igdbId, UUID user) {
        return findByUserUUIDAndIgdbId(igdbId, user);
    }

    @Transactional
    public UserGame add(Long igdbId, UUID user) {
        log.info("Attempting to add game with igdbId={} for user '{}'", igdbId, user);
        Optional<UserGame> userGame = userGameRepository.findUserGameByGame_IgdbIdAndUser(igdbId, user);
        if (userGame.isPresent()) {
            log.warn("Game with igdbId={} is already added for user '{}'", igdbId, user);
            return userGame.get();
        }
        Optional<Game> game = gameRepository.findById(igdbId);
        if (game.isEmpty()) {
            log.info("Game with igdbId={} not found in the local database, attempting to fetch via GameService", igdbId);
            game = Optional.ofNullable(gameService.add(igdbId));
            if (game.isEmpty()) {
                log.error("Game with igdbId={} could not be found locally or via GameService", igdbId);
                return null;
            }
            else {
                log.info("Game with igdbId={} successfully fetched via GameService", igdbId);
            }
        }

        UserGame saved = userGameRepository.save(new UserGame(user, game.get()));
        log.info("Game with igdbId={} successfully added for user '{}'", igdbId, user);
        return saved;
    }

    @Transactional
    public UserGame update(Long igdbId, UUID user, UserGameUpdateForm userGameUpdateForm) {
        log.info("Attempting to update UserGame with id={} using data: status={}, rating={}, notes={}",
                igdbId,
                userGameUpdateForm.status(),
                userGameUpdateForm.userRating(),
                userGameUpdateForm.note());

        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);

        log.info("Found UserGame with id={} for user '{}', game title='{}'",
                userGame.getId(),
                userGame.getUser(),
                userGame.getGame().getTitle());

        userGame.updateDto(userGameUpdateForm);

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated UserGame with id={} for user '{}'", saved.getId(), saved.getUser());

        if (saved.getStatus().equals(GameStatus.Completed)) {
            userGameEventProducer.handleUserGameCompleted(
                    new UserGameEvent(user, igdbId, UserGameEvent.EventType.USER_GAME_COMPLETED));
        }

        return saved;
    }

    @Transactional
    public UserGame updateStatus(Long igdbId, UUID user, GameStatus status) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        userGame.setStatus(status);

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        userGame.setUpdatedAt(offsetDateTime.toInstant());

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated status for UserGame with id={} for user '{}'", saved.getId(), saved.getUser());

        if (saved.getStatus().equals(GameStatus.Completed)) {
            userGameEventProducer.handleUserGameCompleted
                    (new UserGameEvent(user, userGame.getId(), UserGameEvent.EventType.USER_GAME_COMPLETED));
        }

        return saved;
    }

    @Transactional
    public UserGame updateFullyCompleted(Long igdbId, UUID user, Boolean fullyCompleted) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        userGame.setFullyCompleted(fullyCompleted);

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        userGame.setUpdatedAt(offsetDateTime.toInstant());

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated isFullyCompleted for UserGame with id={} for user '{}'", saved.getId(), saved.getUser());

        return saved;
    }

    @Transactional
    public UserGame updateRating(Long igdbId, UUID user, Double rating) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        userGame.setUserRating(rating);

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        userGame.setUpdatedAt(offsetDateTime.toInstant());

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated rating for UserGame with id={} for user '{}'", saved.getId(), saved.getUser());

        return saved;
    }

    @Transactional
    public UserGame updateReview(Long igdbId, UUID user, String review) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        userGame.setReview(review);

        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        userGame.setUpdatedAt(offsetDateTime.toInstant());

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated review for UserGame with id={} for user '{}'", saved.getId(), saved.getUser());

        return saved;
    }

    @Transactional
    public void delete(Long igdbId, UUID user) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        log.info("Deleting UserGame with IGDB ID {} for user '{}'", igdbId, user);
        int deleted = userGameRepository.deleteUserGameByGame_IgdbIdAndUser(igdbId, user);

        if (deleted == 1) {
            log.info("Successfully deleted UserGame with IGDB ID {} for user '{}'", igdbId, user);
        } else {
            log.warn("No UserGame found to delete with IGDB ID {} for user '{}'", igdbId, user);
            throw new IllegalArgumentException("UserGame not found.");
        }
    }

    private UserGame findByUserUUIDAndIgdbId(Long igdbId, UUID user) {
        return userGameRepository.findUserGameByGame_IgdbIdAndUser(igdbId, user)
                .orElseThrow(() -> {
                    log.error("UserGame with id={} not found", igdbId);
                    return new EntityNotFoundException("UserGame not found");
                });
    }

    public boolean isContains(Long igdbId, UUID user) {
        return userGameRepository.existsByGame_IgdbIdAndUser(igdbId, user);
    }
}
