package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.NotNull;

public record HandoffShiftRequest(
        @NotNull Long fromAgentId,
        @NotNull Long toAgentId,
        String notes
) {
}
