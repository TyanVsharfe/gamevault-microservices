package com.gamevault.usergameservice.dto.input.update;

import com.gamevault.enums.GameStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateForm(
        @NotNull @Enumerated(EnumType.STRING) GameStatus status
) {}
