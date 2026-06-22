package com.gamevault.usergameservice.service;

import com.gamevault.usergameservice.db.model.Game;
import com.gamevault.usergameservice.db.model.UserGameList;
import com.gamevault.usergameservice.db.repository.UserGameListItemRepository;
import com.gamevault.usergameservice.db.repository.UserGameListRepository;
import com.gamevault.usergameservice.dto.input.UserGameListForm;
import com.gamevault.usergameservice.dto.input.update.UpdateOrderDto;
import com.gamevault.usergameservice.dto.input.update.UserGameListUpdateForm;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserGameListService {
    private final UserGameListRepository userGameListRepository;
    private final UserGameListItemRepository userGameListItemRepository;
    private final GameService gameService;

    public UserGameListService(UserGameListRepository userGameListRepository,
                               UserGameListItemRepository userGameListItemRepository,
                               GameService gameService) {
        this.userGameListRepository = userGameListRepository;
        this.userGameListItemRepository = userGameListItemRepository;
        this.gameService = gameService;
    }
    
    public UserGameList createList(UserGameListForm userGameListForm, UUID authorId, String authorUsername) {
        UserGameList userGameList = new UserGameList(authorId, authorUsername, userGameListForm);
        UserGameList saved = userGameListRepository.save(userGameList);

        log.info("GameList with name={} successfully added for user '{}'", saved.getName(), saved.getAuthorUsername());
        return saved;
    }

    @Transactional
    public void removeGameFromList(UUID listId, Long igdbId, UUID user) {
        UserGameList userGameList = getGameListById(listId, user);
        validateOwnership(userGameList, user);

        Game game = gameService.getOrCreate(igdbId);
        userGameList.removeGame(game);

        userGameListRepository.save(userGameList);
    }

    @Transactional
    public void removeGamesFromList(UUID listId, List<Long> igdbIds, UUID user) {
        UserGameList userGameList = getGameListById(listId, user);
        validateOwnership(userGameList, user);

        for (long igdbId: igdbIds) {
            Game game = gameService.getOrCreate(igdbId);
            userGameList.removeGame(game);
        }

        userGameListRepository.save(userGameList);
    }

    @Transactional
    public UserGameList updateGameList(UUID listId, UserGameListUpdateForm form, UUID user) {
        UserGameList userGameList = getGameListById(listId, user);
        validateOwnership(userGameList, user);

        userGameList.update(form);

        if (form.games() != null && !form.games().isEmpty()) {
            addGamesToList(userGameList, form.games());
        }

        if (form.newOrder() != null && !form.newOrder().isEmpty()) {
            updateGamesOrder(listId, form.newOrder(),user);
        }

        userGameList.updateTimestamp();
        return userGameListRepository.save(userGameList);
    }

    public UserGameList updateGamesOrder(UUID listId, List<UpdateOrderDto> order, UUID user) {
        validateOwnershipByListId(listId, user);
        userGameListItemRepository.updateOrderBatch(listId, order);
        return userGameListRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("Game list not found"));
    }

    @Transactional
    public UserGameList copyGameList(UUID listId, UUID user, String authorUsername) {
        UserGameList originalList = getGameListById(listId, user);

        if (!originalList.isPublic() && !originalList.isOwnedBy(user)) {
            throw new AccessDeniedException("This list is private");
        }

        UserGameList copiedList = new UserGameList(user, authorUsername, originalList);
        copiedList = userGameListRepository.save(copiedList);

        List<Long> gameIds = originalList.getItems().stream()
                .map(item -> item.getGame().getIgdbId())
                .toList();

        return addGamesToList(copiedList, gameIds);
    }

    @Transactional
    public UserGameList addGamesToList(UserGameList userGameList, List<Long> igdbIds) {
        List<Game> games = gameService.getOrCreateBatch(igdbIds);

        int order = userGameList.getItems().size();
        log.info("GameList with id={} will receive '{}' games", userGameList.getUuid(), games.size());
        for (Game game : games) {
            boolean exists = userGameList.getItems().stream()
                    .anyMatch(item -> item.getGame().getIgdbId().equals(game.getIgdbId()));

            if (!exists) {
                userGameList.addGame(game, order++);
                log.info("GameListItem with igdbId={} successfully added for user '{}'", game.getIgdbId(), userGameList.getAuthorUsername());
            }
        }

        return userGameListRepository.save(userGameList);
    }

    @Transactional(readOnly = true)
    public Page<UserGameList> getUserLists(UUID user, Pageable pageable) {
        return userGameListRepository.findByAuthorId(user, pageable);
    }

    @Transactional(readOnly = true)
    public List<UserGameList> getPublicLists(UUID user) {
        return userGameListRepository.findByIsPublicTrueAndAuthorId(user);
    }

    @Transactional(readOnly = true)
    public UserGameList getGameListById(UUID listId, UUID user) {
        UserGameList list = userGameListRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("Game list not found"));
        if (!list.isPublic()) {
            validateOwnership(list, user);
        }
        return list;
    }

    @Transactional
    public void deleteGameList(UUID listId, UUID user)  {
        UserGameList userGameList = getGameListById(listId, user);
        validateOwnership(userGameList, user);
        userGameListRepository.delete(userGameList);
    }

    private void validateOwnership(UserGameList userGameList, UUID user) {
        if (!userGameList.isOwnedBy(user)) {
            throw new AccessDeniedException("You don't have permission to modify this list");
        }
    }

    private void validateOwnershipByListId(UUID listId, UUID user) {
        if (!userGameListRepository.existsByUuidAndAuthorId(listId, user)) {
            throw new AccessDeniedException("You don't have permission to modify this list");
        }
    }
}
