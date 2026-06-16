package com.gamevault.usergameservice.dto.input;

import org.springframework.lang.Nullable;

public record UserSteamSettings(
        @Nullable String steamId,
        @Nullable Boolean steamSyncEnabled
) {}
