package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.VehicleMaintenanceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateVehicleMaintenanceRequest(
        @NotNull VehicleMaintenanceType type,
        LocalDate serviceDate,
        Long kmAtService,
        Long nextMaintenanceKm,
        @DecimalMin("0.0") BigDecimal costAmount,
        String supplierName,
        @NotBlank String description,
        boolean resolved
) {
}
