package com.seguranca.plataforma.organization.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateHrEmployeeAssignmentRequest(
        Long businessUnitId,
        Long businessSectorId,
        @NotBlank String roleTitle,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String notes
) {
}
