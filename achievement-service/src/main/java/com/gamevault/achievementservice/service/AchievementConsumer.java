package com.gamevault.achievementservice.service;

import com.gamevault.events.user.UserEvent;
import com.gamevault.events.user.UserGameEvent;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class AchievementConsumer {
    private final AchievementService achievementService;
    private final AchievementProcessorService achievementProcessorService;

    public AchievementConsumer(AchievementService achievementService, AchievementProcessorService achievementProcessorService) {
        this.achievementService = achievementService;
        this.achievementProcessorService = achievementProcessorService;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-event}", groupId = "achievement-service")
    public void processUserCreatedEvent(UserEvent event) {
        if (event.type().equals(UserEvent.EventType.USER_CREATED)) {
            achievementService.initializeUserAchievements(event.user_id());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.user-game-event}", groupId = "achievement-service")
    public void processUserGameEvent(UserGameEvent event) {
        achievementProcessorService.processAchievementCompletion(event.user_id());
    }
}
