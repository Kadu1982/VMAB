package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RespondShiftHandoffRequest(
        @NotNull Long actingAgentId,
        @Size(max = 500) String notes
) {
}
