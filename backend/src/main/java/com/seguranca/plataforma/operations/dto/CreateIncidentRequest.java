package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.IncidentPriority;
import com.seguranca.plataforma.operations.model.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateIncidentRequest(
        @NotNull IncidentType type,
        @NotNull IncidentPriority priority,
        @NotBlank String residentName,
        @NotBlank String address,
        Long assignedAgentId,
        Long vehicleId
) {
}
