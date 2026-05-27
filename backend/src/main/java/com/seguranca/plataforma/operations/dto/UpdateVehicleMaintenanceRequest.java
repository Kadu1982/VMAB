package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.VehicleMaintenanceType;
import com.seguranca.plataforma.operations.model.VehicleMaintenancePriority;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateVehicleMaintenanceRequest(
        @NotNull VehicleMaintenanceType type,
        @NotNull VehicleMaintenancePriority priority,
        @NotNull VehicleMaintenanceStatus status,
        LocalDate serviceDate,
        LocalDate dueDate,
        Long kmAtService,
        Long nextMaintenanceKm,
        @DecimalMin("0.0") BigDecimal costAmount,
        String supplierName,
        String resolutionNotes,
        @NotBlank String description,
        boolean resolved
) {
}


