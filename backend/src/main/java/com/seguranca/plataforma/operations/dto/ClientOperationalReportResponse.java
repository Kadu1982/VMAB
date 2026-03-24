package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.Incident;
import java.time.OffsetDateTime;
import java.util.List;

public record ClientOperationalReportResponse(
        OffsetDateTime generatedAt,
        long activeShifts,
        long openIncidents,
        long availableVehicles,
        long maintenanceAlerts,
        long openMaintenanceOrders,
        long criticalMaintenanceOrders,
        long lateShifts,
        long absentShifts,
        double averageDispatchMinutes,
        double averageResolutionMinutes,
        List<Incident> incidents,
        List<VehicleMaintenanceOrderResponse> maintenanceOrders
) {
}
