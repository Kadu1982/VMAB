package com.seguranca.plataforma.organization.dto;

import com.seguranca.plataforma.organization.model.BusinessUnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBusinessUnitRequest(
        @NotBlank String name,
        @NotNull BusinessUnitType type,
        String cnpj,
        boolean active,
        String notes
) {
}


