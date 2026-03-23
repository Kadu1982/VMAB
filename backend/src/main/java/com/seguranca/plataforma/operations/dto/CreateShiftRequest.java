package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateShiftRequest(
        @NotNull Long agentId,
        @NotNull Long vehicleId,
        @NotNull OffsetDateTime scheduledEndAt,
        Integer fuelLevelPercent,
        boolean tiresChecked,
        boolean lightsChecked,
        boolean documentsChecked,
        String checklistNotes
) {
}
