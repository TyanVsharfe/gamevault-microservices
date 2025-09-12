package com.gamevault.usergameservice.service;

import com.gamevault.events.user.UserGameEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserGameEventProducer {

    private final KafkaTemplate<String, UserGameEvent> kafkaTemplate;

    @Value("${app.kafka.topics.user-game-event}")
    private String userEventTopic;

    public UserGameEventProducer(KafkaTemplate<String, UserGameEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void handleUserGameCompleted(UserGameEvent event) {
        kafkaTemplate.send(userEventTopic, event);
    }
}
