package com.gamevault.usergameservice.service;

import com.gamevault.dto.usergame.UserGameSnapshotItem;
import com.gamevault.enums.IgdbGameType;
import com.gamevault.events.user.UserGameEvent;
import com.gamevault.usergameservice.db.model.Game;
import com.gamevault.usergameservice.db.model.UserCache;
import com.gamevault.usergameservice.db.model.UserGame;
import com.gamevault.usergameservice.db.repository.GameRepository;
import com.gamevault.usergameservice.db.repository.UserCacheRepository;
import com.gamevault.usergameservice.db.repository.UserGameCustomRepository;
import com.gamevault.usergameservice.db.repository.UserGameRepository;
import com.gamevault.usergameservice.dto.input.UserGamesFilterParams;
import com.gamevault.usergameservice.dto.input.update.UserGameUpdateForm;
import com.gamevault.usergameservice.dto.output.UserReviewsDTO;
import com.gamevault.enums.GameStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGameService {
    private final UserGameRepository userGameRepository;
    private final GameRepository gameRepository;
    private final UserCacheRepository userCacheRepository;
    private final GameService gameService;
    private final UserGameEventProducer userGameEventProducer;
    private final UserGameCustomRepository userGameCustomRepository;

    public Page<UserGame> getAll(UUID author, Pageable pageable, UserGamesFilterParams filterParams) {
        return userGameCustomRepository.findGamesWithFilters(filterParams, author, pageable);
    }

    public List<UserReviewsDTO> getGameReviews(Long igdbId) {
        List<UserGame> reviews = userGameRepository.findByGameIgdbIdAndReviewIsNotNull(igdbId);
        return reviews.stream()
                .filter(review -> !review.getReview().isEmpty())
                .map(r -> {
                    String username = userCacheRepository.findById(r.getUserId()).map(UserCache::getUsername).orElse("Anonymous");
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
        Optional<UserGame> userGame = userGameRepository.findUserGameByGame_IgdbIdAndUserId(igdbId, user);
        if (userGame.isPresent()) {
            log.warn("Game with igdbId={} is already added for user '{}'", igdbId, user);
            return userGame.get();
        }

        Game game = gameService.getOrCreate(igdbId);

        UserGame saved;
        if (game.getCategory() == IgdbGameType.DLC || game.getCategory() == IgdbGameType.EXPANSION) {
            Optional<UserGame> parentGame = Optional.ofNullable(game.getParentGame())
                    .flatMap(parent -> userGameRepository.findUserGameByGame_IgdbIdAndUserId(parent.getIgdbId(), user));

            if (parentGame.isPresent()) {
                saved = userGameRepository.save(new UserGame(user, game, parentGame.get()));
                log.warn("Game with igdbId={} is already added for user with Id '{}'", igdbId, user);
            }
            else {
                String message = game.getParentGame() != null
                        ? "DLC cannot be added because the parent game is not added for user"
                        : "DLC cannot be added because it has no parent game";
                log.warn("DLC with igdbId={} not added: {}", igdbId, message);
                throw new EntityNotFoundException(message);
            }
        }
        else {
            saved = userGameRepository.save(new UserGame(user, game));
        }

        log.info("Game with igdbId={} successfully added for user '{}'", igdbId, user);
        publishUpsert(saved);
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
                userGame.getUserId(),
                userGame.getGame().getTitle());

        userGame.updateDto(userGameUpdateForm);

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated UserGame with id={} for user '{}'", saved.getId(), saved.getUserId());

        if (userGameUpdateForm.status() != null) {
            publishUpsert(saved);
        }

        return saved;
    }

    @Transactional
    public UserGame updateStatus(Long igdbId, UUID user, GameStatus status) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        userGame.setStatus(status);

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated status for UserGame with id={} for user '{}'", saved.getId(), saved.getUserId());

        publishUpsert(saved);

        return saved;
    }

    @Transactional
    public UserGame updateFullyCompleted(Long igdbId, UUID user, Boolean fullyCompleted) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        userGame.setFullyCompleted(fullyCompleted);

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated isFullyCompleted for UserGame with id={} for user '{}'", saved.getId(), saved.getUserId());

        return saved;
    }

    @Transactional
    public UserGame updateRating(Long igdbId, UUID user, Double rating) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        userGame.setOverallRating(rating);

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated rating for UserGame with id={} for user '{}'", saved.getId(), saved.getUserId());

        return saved;
    }

    @Transactional
    public UserGame updateReview(Long igdbId, UUID user, String review) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        userGame.setReview(review);

        UserGame saved = userGameRepository.save(userGame);
        log.info("Successfully updated review for UserGame with id={} for user '{}'", saved.getId(), saved.getUserId());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<UserGameSnapshotItem> getSnapshot(UUID userId, Pageable pageable) {
        return userGameRepository.findSnapshotByUserId(userId, pageable);
    }

    @Transactional
    public void delete(Long igdbId, UUID user) {
        UserGame userGame = findByUserUUIDAndIgdbId(igdbId, user);
        log.info("Deleting UserGame with IGDB ID {} for user '{}'", igdbId, user);
        int deleted = userGameRepository.deleteUserGameByGame_IgdbIdAndUserId(igdbId, user);

        if (deleted == 1) {
            log.info("Successfully deleted UserGame with IGDB ID {} for user '{}'", igdbId, user);
            userGameEventProducer.publish(
                    new UserGameEvent(user, igdbId, null, UserGameEvent.EventType.USER_GAME_DELETED));
        } else {
            log.warn("No UserGame found to delete with IGDB ID {} for user '{}'", igdbId, user);
            throw new IllegalArgumentException("UserGame not found.");
        }
    }

    private UserGame findByUserUUIDAndIgdbId(Long igdbId, UUID user) {
        return userGameRepository.findUserGameByGame_IgdbIdAndUserId(igdbId, user)
                .orElseThrow(() -> {
                    log.error("UserGame with id={} not found", igdbId);
                    return new EntityNotFoundException("UserGame not found");
                });
    }

    private void publishUpsert(UserGame userGame) {
        userGameEventProducer.publish(new UserGameEvent(
                userGame.getUserId(),
                userGame.getGame().getIgdbId(),
                userGame.getStatus(),
                UserGameEvent.EventType.USER_GAME_UPSERTED
        ));
    }
}
