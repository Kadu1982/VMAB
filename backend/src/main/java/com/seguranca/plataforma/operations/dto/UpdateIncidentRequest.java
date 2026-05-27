package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.IncidentPriority;
import com.seguranca.plataforma.operations.model.IncidentStatus;
import com.seguranca.plataforma.operations.model.IncidentType;
import jakarta.validation.constraints.NotNull;

public record UpdateIncidentRequest(
        @NotNull IncidentType type,
        @NotNull IncidentPriority priority,
        @NotNull IncidentStatus status,
        Long residentId,
        String residentName,
        String address,
        Long assignedAgentId,
        Long vehicleId
) {
}


