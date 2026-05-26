package com.seguranca.plataforma.hr.dto;

import com.seguranca.plataforma.hr.model.HrEmployeeCategory;
import com.seguranca.plataforma.hr.model.HrEmployeeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateHrEmployeeRequest(
        @NotBlank String employeeCode,
        @NotBlank String fullName,
        @NotNull HrEmployeeCategory category,
        @NotNull HrEmployeeStatus status,
        String documentNumber,
        String phoneNumber,
        String email,
        String photoUrl,
        String cnhCategory,
        LocalDate cnhExpiry,
        LocalDate medicalExamExpiry,
        LocalDate trainingExpiry,
        String trainingNotes,
        String documentNotes,
        LocalDate hireDate,
        LocalDate terminationDate,
        Long linkedAgentId,
        Long linkedAppUserId,
        boolean pointEnabled
) {
}
