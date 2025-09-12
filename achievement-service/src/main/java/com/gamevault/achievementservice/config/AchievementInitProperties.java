package com.gamevault.achievementservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "init.achievements")
@Getter
@Setter
public class AchievementInitProperties {
    private boolean enabled = true;
    private int batchSize = 1000;
    private String authServiceUrl;
    private String usersEndpoint = "/api/users/uuids";
}
