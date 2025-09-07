package com.gamevault.authservice.service;

import com.gamevault.events.user.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserProducer {
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.user-event}")
    private String userEventTopic;

    public UserProducer(KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void InitializeUserAchievements(UserCreatedEvent event) {
        log.info("Init achievements for user {}", event.user_id());
        kafkaTemplate.send(userEventTopic, event);
    }
}
