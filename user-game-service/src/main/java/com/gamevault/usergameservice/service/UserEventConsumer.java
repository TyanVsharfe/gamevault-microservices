package com.gamevault.usergameservice.service;

import com.gamevault.events.user.UserEvent;
import com.gamevault.usergameservice.db.model.UserCache;
import com.gamevault.usergameservice.db.repository.UserCacheRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {
    private final UserCacheRepository userCacheRepository;

    public UserEventConsumer(UserCacheRepository userCacheRepository) {
        this.userCacheRepository = userCacheRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-event}", groupId = "usergame-service")
    public void consume(UserEvent event) {
        switch (event.type()) {
            case USER_CREATED, USER_UPDATED -> {
                UserCache userCache = new UserCache(
                        event.user_id(),
                        event.username(),
                        event.avatar_url()
                );
                userCacheRepository.save(userCache);
            }
            case USER_DELETED -> {
                userCacheRepository.deleteById(event.user_id());
            }
        }
    }
}
