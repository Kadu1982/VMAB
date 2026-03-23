package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateVehicleRequest(
        @NotBlank String plate,
        @NotBlank String model,
        @Min(0) long currentKm,
        @Min(1) long nextMaintenanceKm
) {
}
