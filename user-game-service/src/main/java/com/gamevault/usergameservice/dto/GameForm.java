package com.gamevault.usergameservice.dto;

import com.gamevault.enums.GameModesIGDB;
import com.gamevault.enums.IgdbGameType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GameForm(
        @NotNull @Min(1) Long igdbId,
        @NotNull @Size(max = 1000) String title,
        @NotNull @Size(max = 2000) String coverUrl,
        @NotNull @Size(max = 10000) String description,
        @NotNull @Enumerated(EnumType.ORDINAL) IgdbGameType category,
        @NotNull @Enumerated(EnumType.ORDINAL) List<GameModesIGDB> gameModes
) {}

