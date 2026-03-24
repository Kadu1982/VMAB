package com.seguranca.plataforma.operations.residentapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResidentLoginRequest(
        @NotNull Long residentId,
        @NotBlank String phoneNumber
) {
}
