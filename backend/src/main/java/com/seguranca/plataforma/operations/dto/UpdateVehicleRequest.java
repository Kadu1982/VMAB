package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.VehicleStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateVehicleRequest(
        @NotBlank String plate,
        @NotBlank String model,
        @Min(0) long currentKm,
        @Min(0) Long nextMaintenanceKm,
        @NotNull VehicleStatus status,
        LocalDate ipvaExpiry,
        LocalDate licensingExpiry,
        LocalDate insuranceExpiry,
        LocalDate lastMaintenanceAt,
        String maintenanceNotes
) {
}
