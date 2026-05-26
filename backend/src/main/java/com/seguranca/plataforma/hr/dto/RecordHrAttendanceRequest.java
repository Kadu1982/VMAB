package com.seguranca.plataforma.hr.dto;

import com.seguranca.plataforma.hr.model.HrAttendanceType;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record RecordHrAttendanceRequest(
        @NotNull HrAttendanceType eventType,
        @NotNull OffsetDateTime occurredAt,
        String deviceLabel,
        Double latitude,
        Double longitude,
        String note,
        boolean anomalyFlag,
        String anomalyReason
) {
}
