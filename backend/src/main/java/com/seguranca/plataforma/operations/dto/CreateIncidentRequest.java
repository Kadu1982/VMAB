package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.IncidentPriority;
import com.seguranca.plataforma.operations.model.IncidentType;
import jakarta.validation.constraints.NotNull;

public record CreateIncidentRequest(
        @NotNull IncidentType type,
        @NotNull IncidentPriority priority,
        Long residentId,
        String residentName,
        String address,
        Long assignedAgentId,
        Long vehicleId
) {
}


