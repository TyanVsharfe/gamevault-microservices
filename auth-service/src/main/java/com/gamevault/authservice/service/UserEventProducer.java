package com.gamevault.authservice.service;

import com.gamevault.events.user.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserEventProducer {
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${app.kafka.topics.user-event}")
    private String userEventTopic;

    public UserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void InitializeUserAchievements(UserEvent event) {
        log.info("Init achievements for user {}", event.user_id());
        kafkaTemplate.send(userEventTopic, event);
    }
}
