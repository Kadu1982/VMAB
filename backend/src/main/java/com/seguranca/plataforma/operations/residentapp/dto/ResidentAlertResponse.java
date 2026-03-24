package com.seguranca.plataforma.operations.residentapp.dto;

import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertStatus;
import com.seguranca.plataforma.operations.residentapp.model.ResidentAlertType;
import java.time.OffsetDateTime;

public record ResidentAlertResponse(
        Long id,
        Long residentId,
        String residentName,
        String residentPhoneNumber,
        String residentAddress,
        ResidentAlertType type,
        ResidentAlertStatus status,
        Double latitude,
        Double longitude,
        String notes,
        OffsetDateTime openedAt,
        OffsetDateTime updatedAt,
        OffsetDateTime acknowledgedAt,
        OffsetDateTime dispatchedAt,
        OffsetDateTime onSiteAt,
        OffsetDateTime resolvedAt,
        OffsetDateTime cancelledAt,
        Long assignedAgentId,
        String assignedAgentName,
        Long vehicleId,
        String vehiclePlate,
        String acknowledgmentNotes,
        String dispatchNotes,
        String arrivalNotes,
        String resolutionNotes,
        String cancellationReason
) {
}
