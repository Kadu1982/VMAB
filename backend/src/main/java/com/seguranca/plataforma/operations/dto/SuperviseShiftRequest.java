package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.Size;

public record SuperviseShiftRequest(
        ShiftSupervisionAction action,
        Long replacementAgentId,
        Integer lateMinutes,
        @Size(max = 500) String notes
) {
}


