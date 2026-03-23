package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.ResidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateResidentRequest(
        @NotBlank String fullName,
        @NotBlank String phoneNumber,
        @NotBlank String address,
        String referenceNote,
        @NotNull ResidentStatus status
) {
}
