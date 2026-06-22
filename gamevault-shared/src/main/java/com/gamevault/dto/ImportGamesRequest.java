package com.gamevault.dto;

import java.util.List;

public record ImportGamesRequest(
        List<Long> igdbIds
) {}
