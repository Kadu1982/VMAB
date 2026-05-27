package com.seguranca.plataforma.hr.dto;

import java.util.List;

public record HrSummaryResponse(
        long totalEmployees,
        long activeEmployees,
        long blockedEmployees,
        long checkedInNowEmployees,
        long expiringSoonAlerts,
        List<HrEmployeeResponse> employees,
        List<HrEmployeeAlertResponse> alerts,
        List<HrAttendanceResponse> attendance
) {
}


