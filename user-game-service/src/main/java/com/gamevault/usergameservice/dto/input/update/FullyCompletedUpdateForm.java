package com.gamevault.usergameservice.dto.input.update;

import jakarta.validation.constraints.NotNull;

public record FullyCompletedUpdateForm(
        @NotNull Boolean fullyCompleted
) {}
