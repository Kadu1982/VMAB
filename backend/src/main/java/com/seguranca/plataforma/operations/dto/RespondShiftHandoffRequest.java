package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.Size;

public record RespondShiftHandoffRequest(
        @Size(max = 500) String notes,
        @Size(max = 500) String rejectionReason
) {
}
