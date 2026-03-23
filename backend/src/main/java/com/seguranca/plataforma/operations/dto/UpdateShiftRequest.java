package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.ShiftStatus;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record UpdateShiftRequest(
        @NotNull Long agentId,
        @NotNull Long vehicleId,
        @NotNull ShiftStatus status,
        @NotNull OffsetDateTime scheduledEndAt
) {
}
