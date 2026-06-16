package com.gamevault.dto.igdb;

import java.util.List;

public record Series(
        int id,
        String name,
        String slug,
        List<IgdbGameDto> games
) {}
