package com.gamevault.dto;

import com.gamevault.dto.igdb.IgdbGameDto;

public record IgdbSteamMatchDto(
        Long steamAppId,
        IgdbGameDto game
) {}