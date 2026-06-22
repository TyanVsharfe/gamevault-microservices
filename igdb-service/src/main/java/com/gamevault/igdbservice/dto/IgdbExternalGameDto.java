package com.gamevault.igdbservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gamevault.dto.igdb.IgdbGameDto;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IgdbExternalGameDto(
        String uid,
        IgdbGameDto game
) {}
