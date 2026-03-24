package com.seguranca.plataforma.operations.dto;

import java.math.BigDecimal;
import java.util.List;

// Resumo operacional da frota usado para gestoes, relatorios e analise de risco.
public record FleetOperationalReportResponse(
        long totalVehicles,
        long operationalVehicles,
        long availableVehicles,
        long inOperationVehicles,
        long maintenanceVehicles,
        long blockedVehicles,
        long maintenanceDueSoonVehicles,
        long maintenanceOverdueVehicles,
        long documentAlertVehicles,
        long maintenanceOrdersOpen,
        long maintenanceOrdersResolved,
        long preventiveOrdersOpen,
        long correctiveOrdersOpen,
        long inspectionOrdersOpen,
        long documentationOrdersOpen,
        BigDecimal totalMaintenanceCostLast30Days,
        BigDecimal totalMaintenanceCostAllTime,
        List<VehicleMaintenanceOrderResponse> latestOrders
) {
}
