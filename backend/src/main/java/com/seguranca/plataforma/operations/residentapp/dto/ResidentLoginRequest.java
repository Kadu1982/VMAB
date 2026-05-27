package com.seguranca.plataforma.operations.residentapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResidentLoginRequest(
        @NotNull Long residentId,
        @NotBlank @Pattern(regexp = "^\\d{4,6}$", message = "O PIN deve ter entre 4 e 6 digitos.") String accessPin
) {
}


