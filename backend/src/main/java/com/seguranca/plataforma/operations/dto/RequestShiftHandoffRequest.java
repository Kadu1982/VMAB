package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RequestShiftHandoffRequest(
        @NotNull Long toAgentId,
        @Size(max = 500) String notes
) {
}
