package com.gamevault.usergameservice.dto.input;

import com.gamevault.enums.SteamSync;
import org.springframework.lang.Nullable;

import java.util.Set;

public record UserSteamSync(
        @Nullable Boolean steamSyncEnabled,
        @Nullable Set<Long> ignoredGameIds,
        @Nullable SteamSync syncFrequency
) {}
