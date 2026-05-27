package com.seguranca.plataforma.hr.dto;

import java.time.LocalDate;

public record HrEmployeeAlertResponse(
        Long employeeId,
        String employeeName,
        String alertType,
        LocalDate dueDate,
        long daysRemaining,
        String message
) {
}


