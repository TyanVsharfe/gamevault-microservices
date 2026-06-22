package com.gamevault.usergameservice.service;

import com.gamevault.enums.ImportItemStatus;
import com.gamevault.events.user.UserGameEvent;
import com.gamevault.usergameservice.db.model.UserGame;
import com.gamevault.usergameservice.db.repository.UserGameRepository;
import com.gamevault.dto.ImportGameItemResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserGameImportItemService {
    private final UserGameRepository userGameRepository;
    private final UserGameService gameService;
    private final UserGameEventProducer eventProducer;

    public UserGameImportItemService(UserGameRepository userGameRepository, UserGameService gameService, UserGameEventProducer eventProducer) {
        this.userGameRepository = userGameRepository;
        this.gameService = gameService;
        this.eventProducer = eventProducer;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportGameItemResult addOne(UUID userId, Long igdbId) {
        Optional<UserGame> userGame = userGameRepository.findUserGameByGame_IgdbIdAndUserId(igdbId, userId);

        if (userGame.isPresent()) {
            return new ImportGameItemResult(igdbId, ImportItemStatus.ALREADY_EXISTS, null, null);
        }

        UserGame saved = gameService.add(igdbId, userId);

        eventProducer.publish(new UserGameEvent(
                userId,
                saved.getGame().getIgdbId(),
                saved.getStatus(),
                UserGameEvent.EventType.USER_GAME_UPSERTED
        ));

        return new ImportGameItemResult(igdbId, ImportItemStatus.ADDED, null, null);
    }
}
