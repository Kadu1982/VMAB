package com.seguranca.plataforma.operations.model;

import java.time.OffsetDateTime;

public record Shift(
        Long id,
        Long agentId,
        String agentName,
        Long vehicleId,
        String vehiclePlate,
        ShiftStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime scheduledEndAt
) {
}
