package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.ResidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateResidentRequest(
        @NotBlank String fullName,
        @NotBlank String phoneNumber,
        @NotBlank String address,
        String cpf,
        String photoUrl,
        Long businessUnitId,
        String referenceNote,
        @NotNull ResidentStatus status,
        @Pattern(regexp = "^\\d{4,6}$", message = "O PIN de acesso deve ter entre 4 e 6 digitos.") String accessPin,
        @Pattern(regexp = "^\\d{4,6}$", message = "O PIN de coacao deve ter entre 4 e 6 digitos.") String coercionPin
) {
}


