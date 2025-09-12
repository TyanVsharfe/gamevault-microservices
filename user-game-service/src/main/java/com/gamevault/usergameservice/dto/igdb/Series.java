package com.gamevault.usergameservice.dto.igdb;

import java.util.List;
public record Series(
        int id,
        String name,
        List<IgdbGameDTO> games
) {}
