package com.gamevault.usergameservice.dto.input.update;

import com.gamevault.enums.GameStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.lang.Nullable;

public record UserGameModeUpdateForm(
        @Nullable @Enumerated(EnumType.STRING) GameStatus status,
        @Nullable @DecimalMin("0.0") @DecimalMax("100.0") Double userRating
) {}
