package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.NotNull;

public record DispatchIncidentRequest(
        @NotNull Long assignedAgentId,
        @NotNull Long vehicleId,
        String dispatchNotes
) {
}
