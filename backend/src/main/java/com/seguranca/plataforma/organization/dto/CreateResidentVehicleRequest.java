package com.seguranca.plataforma.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateResidentVehicleRequest(
        @NotBlank String plate,
        String model,
        String color,
        boolean active,
        String notes
) {
}


