package com.seguranca.plataforma.operations.residentapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DispatchResidentAlertRequest(
        @NotNull Long assignedAgentId,
        @NotNull Long vehicleId,
        @Size(max = 500) String dispatchNotes
) {
}


