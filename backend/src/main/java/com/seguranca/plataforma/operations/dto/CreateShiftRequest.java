package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.ShiftAttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.OffsetDateTime;

public record CreateShiftRequest(
        @NotNull Long agentId,
        @NotNull Long vehicleId,
        @NotNull OffsetDateTime scheduledStartAt,
        @NotNull OffsetDateTime scheduledEndAt,
        @Min(0) @Max(100)
        Integer fuelLevelPercent,
        boolean tiresChecked,
        boolean lightsChecked,
        boolean documentsChecked,
        String checklistNotes,
        ShiftAttendanceStatus attendanceStatus,
        Long coverageForAgentId,
        String attendanceNotes
) {
}
