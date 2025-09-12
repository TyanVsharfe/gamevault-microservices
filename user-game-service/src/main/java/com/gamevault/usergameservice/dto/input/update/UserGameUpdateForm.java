package com.gamevault.usergameservice.dto.input.update;

import com.gamevault.enums.GameStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

import java.util.UUID;

public record UserGameUpdateForm(
        @Nullable @Enumerated(EnumType.STRING) GameStatus status,
        @Nullable Boolean isFullyCompleted,
        @Nullable @DecimalMin("0.0") @DecimalMax("100.0") Double userRating,
        @Nullable @Size(max = 10000) String review,
        @Nullable @Valid UUID note
) {}

