package com.gamevault.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamGame(
        Long appid,
        String name,
        @JsonProperty("playtime_forever")
        Long playtimeForever,
        @JsonProperty("img_icon_url")
        String imgIconUrl,
        @JsonProperty("has_community_visible_stats")
        boolean hasCommunityVisibleStats,
        @JsonProperty("playtime_windows_forever")
        Long playtimeWindowsForever,
        @JsonProperty("playtime_mac_forever")
        Long playtimeMacForever,
        @JsonProperty("playtime_linux_forever")
        Long playtimeLinuxForever,
        @JsonProperty("playtime_deck_forever")
        Long playtimeDeckForever,
        @JsonProperty("rtime_last_played")
        Long rTimeLastPlayed,
        @JsonProperty("playtime_disconnected")
        Long playtimeDisconnected
) {}
