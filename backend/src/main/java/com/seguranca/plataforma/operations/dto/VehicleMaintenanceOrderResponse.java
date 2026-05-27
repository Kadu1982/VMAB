package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.VehicleMaintenanceLifecycleStatus;
import com.seguranca.plataforma.operations.model.VehicleMaintenancePriority;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceStatus;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

// Resposta formal de uma ordem de servico da frota para leitura em dashboard e relatorios.
public record VehicleMaintenanceOrderResponse(
        Long id,
        String workOrderCode,
        Long vehicleId,
        String vehiclePlate,
        String vehicleModel,
        VehicleMaintenanceType type,
        VehicleMaintenancePriority priority,
        VehicleMaintenanceStatus status,
        VehicleMaintenanceLifecycleStatus lifecycleStatus,
        String lifecycleLabel,
        OffsetDateTime openedAt,
        OffsetDateTime completedAt,
        LocalDate serviceDate,
        LocalDate dueDate,
        Long kmAtService,
        Long nextMaintenanceKm,
        Long kmRemainingAtService,
        Long daysUntilDue,
        BigDecimal costAmount,
        String supplierName,
        String resolutionNotes,
        String description,
        boolean resolved,
        boolean blockingVehicle,
        long ageDays
) {
}


