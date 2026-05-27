package com.seguranca.plataforma.operations.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateVehicleRequest(
        @NotBlank String plate,
        @NotBlank String model,
        @Min(0) long currentKm,
        @Min(0) Long nextMaintenanceKm,
        LocalDate ipvaExpiry,
        LocalDate licensingExpiry,
        LocalDate insuranceExpiry,
        LocalDate lastMaintenanceAt,
        String maintenanceNotes
) {
}


