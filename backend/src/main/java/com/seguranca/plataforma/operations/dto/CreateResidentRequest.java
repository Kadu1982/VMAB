package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateResidentRequest(
        @NotBlank String fullName,
        @NotBlank String phoneNumber,
        @NotBlank String address,
        String referenceNote
) {
}
