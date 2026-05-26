package com.seguranca.plataforma.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBusinessSectorRequest(
        @NotBlank String name,
        String code,
        boolean active,
        String notes
) {
}
