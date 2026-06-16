package com.gamevault.achievementservice.service;

import com.gamevault.achievementservice.db.model.UserGameCache;
import com.gamevault.achievementservice.db.repository.UserGameCacheRepository;
import com.gamevault.enums.GameStatus;
import com.gamevault.events.user.UserEvent;
import com.gamevault.events.user.UserGameEvent;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class AchievementConsumer {
    private final AchievementService achievementService;
    private final AchievementProcessorService achievementProcessorService;
    private final UserGameCacheRepository userGameRepository;

    public AchievementConsumer(AchievementService achievementService, AchievementProcessorService achievementProcessorService, UserGameCacheRepository userGameRepository) {
        this.achievementService = achievementService;
        this.achievementProcessorService = achievementProcessorService;
        this.userGameRepository = userGameRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-event}", groupId = "achievement-service")
    public void processUserCreatedEvent(UserEvent event) {
        if (event.type().equals(UserEvent.EventType.USER_CREATED)) {
            achievementService.initializeUserAchievements(event.user_id());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.user-game-event}", groupId = "achievement-service")
    public void processUserGameEvent(UserGameEvent event) {
        switch (event.type()) {
            case USER_GAME_ADDED -> upsertAndProcess(event, GameStatus.NONE);
            case USER_GAME_COMPLETED -> upsertAndProcess(event, GameStatus.COMPLETED);
            case USER_GAME_UPSERTED -> upsertAndProcess(event, event.status());
            case USER_GAME_DELETED -> {
                userGameRepository.deleteByGameIdAndUser(event.game_id(), event.user_id());
                achievementProcessorService.processAchievementCompletion(event.user_id());
            }
        }
    }

    private void upsertAndProcess(UserGameEvent event, GameStatus fallbackStatus) {
        GameStatus status = event.status() != null ? event.status() : fallbackStatus;
        UserGameCache userGame = new UserGameCache(event.user_id(), event.game_id(), status);
        userGameRepository.save(userGame);
        achievementProcessorService.processAchievementCompletion(event.user_id());
    }
}
