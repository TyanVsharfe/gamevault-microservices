package com.gamevault.achievementservice.service;

import com.gamevault.events.user.UserCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class AchievementConsumer {
    private final AchievementService achievementService;

    public AchievementConsumer(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-event}", groupId = "achievement")
    public void processUserCreatedEvent(UserCreatedEvent event) {
        achievementService.initializeUserAchievements(event.user_id());
    }
}
