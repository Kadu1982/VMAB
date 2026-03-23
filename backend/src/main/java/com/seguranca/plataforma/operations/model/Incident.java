package com.seguranca.plataforma.operations.model;

import java.time.OffsetDateTime;

public record Incident(
        Long id,
        IncidentType type,
        IncidentPriority priority,
        IncidentStatus status,
        String residentName,
        String address,
        OffsetDateTime openedAt,
        String assignedAgentName,
        String vehiclePlate
) {
}
