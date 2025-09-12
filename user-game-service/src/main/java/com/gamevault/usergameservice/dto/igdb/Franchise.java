package com.gamevault.usergameservice.dto.igdb;

import java.util.List;

public record Franchise(
        int id,
        String name,
        String slug,
        List<FranchiseGame> games
) {}
