package com.gamevault.dto;

import java.util.List;

public record ImportGamesResponse(
        int requested,
        int added,
        int alreadyExists,
        int failed,
        List<ImportGameItemResult> items
) {}
