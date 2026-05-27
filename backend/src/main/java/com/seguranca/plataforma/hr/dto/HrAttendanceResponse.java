package com.seguranca.plataforma.hr.dto;

import com.seguranca.plataforma.hr.model.HrAttendance;
import com.seguranca.plataforma.hr.model.HrAttendanceType;
import java.time.OffsetDateTime;

public record HrAttendanceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        HrAttendanceType eventType,
        OffsetDateTime occurredAt,
        String deviceLabel,
        Double latitude,
        Double longitude,
        String note,
        boolean anomalyFlag,
        String anomalyReason
) {
    public static HrAttendanceResponse fromEntity(HrAttendance attendance, String employeeName) {
        return new HrAttendanceResponse(
                attendance.getId(),
                attendance.getEmployeeId(),
                employeeName,
                attendance.getEventType(),
                attendance.getOccurredAt(),
                attendance.getDeviceLabel(),
                attendance.getLatitude(),
                attendance.getLongitude(),
                attendance.getNote(),
                attendance.isAnomalyFlag(),
                attendance.getAnomalyReason()
        );
    }
}


