package com.seguranca.plataforma.operations.service;

import com.seguranca.plataforma.operations.dto.FleetOperationalReportResponse;
import com.seguranca.plataforma.operations.dto.VehicleMaintenanceOrderResponse;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceLifecycleStatus;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceType;
import com.seguranca.plataforma.operations.model.VehicleStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class VehicleFleetReportCalculator {
    // Concentra as regras de leitura da frota para manter o service principal mais limpo e testavel.

    private static final long MAINTENANCE_ALERT_THRESHOLD_KM = 1_000L;
    private static final int DOCUMENT_ALERT_WINDOW_DAYS = 30;

    public FleetOperationalReportResponse buildReport(List<Vehicle> vehicles, List<VehicleMaintenanceRecord> maintenanceRecords) {
        LocalDate referenceDate = LocalDate.now(ZoneOffset.UTC);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        long totalVehicles = vehicles.size();
        long operationalVehicles = vehicles.stream().filter(Vehicle::isOperationallyReady).count();
        long availableVehicles = vehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE).count();
        long inOperationVehicles = vehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.IN_OPERATION).count();
        long maintenanceVehicles = vehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.MAINTENANCE).count();
        long blockedVehicles = vehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.BLOCKED).count();
        long maintenanceDueSoonVehicles = vehicles.stream().filter(vehicle -> vehicle.isMaintenanceDueSoon(MAINTENANCE_ALERT_THRESHOLD_KM)).count();
        long maintenanceOverdueVehicles = vehicles.stream().filter(Vehicle::isMaintenanceDue).count();
        long documentAlertVehicles = vehicles.stream().filter(vehicle -> vehicle.hasDocumentAlert(referenceDate, DOCUMENT_ALERT_WINDOW_DAYS)).count();

        long maintenanceOrdersOpen = maintenanceRecords.stream().filter(record -> !record.isResolved()).count();
        long maintenanceOrdersResolved = maintenanceRecords.stream().filter(VehicleMaintenanceRecord::isResolved).count();
        long preventiveOrdersOpen = maintenanceRecords.stream()
                .filter(record -> !record.isResolved() && record.getType() == VehicleMaintenanceType.PREVENTIVE)
                .count();
        long correctiveOrdersOpen = maintenanceRecords.stream()
                .filter(record -> !record.isResolved() && record.getType() == VehicleMaintenanceType.CORRECTIVE)
                .count();
        long inspectionOrdersOpen = maintenanceRecords.stream()
                .filter(record -> !record.isResolved() && record.getType() == VehicleMaintenanceType.INSPECTION)
                .count();
        long documentationOrdersOpen = maintenanceRecords.stream()
                .filter(record -> !record.isResolved() && record.getType() == VehicleMaintenanceType.DOCUMENTATION)
                .count();

        BigDecimal totalMaintenanceCostLast30Days = maintenanceRecords.stream()
                .filter(record -> record.getOpenedAt() != null && record.getOpenedAt().isAfter(now.minusDays(30)))
                .map(VehicleMaintenanceRecord::getCostAmount)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMaintenanceCostAllTime = maintenanceRecords.stream()
                .map(VehicleMaintenanceRecord::getCostAmount)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, Vehicle> vehiclesById = vehicles.stream().collect(Collectors.toMap(Vehicle::getId, vehicle -> vehicle));
        List<VehicleMaintenanceOrderResponse> latestOrders = maintenanceRecords.stream()
                .sorted(Comparator.comparing(VehicleMaintenanceRecord::getOpenedAt).reversed())
                .limit(10)
                .map(record -> toOrderResponse(record, vehiclesById.get(record.getVehicleId()), now))
                .toList();

        return new FleetOperationalReportResponse(
                totalVehicles,
                operationalVehicles,
                availableVehicles,
                inOperationVehicles,
                maintenanceVehicles,
                blockedVehicles,
                maintenanceDueSoonVehicles,
                maintenanceOverdueVehicles,
                documentAlertVehicles,
                maintenanceOrdersOpen,
                maintenanceOrdersResolved,
                preventiveOrdersOpen,
                correctiveOrdersOpen,
                inspectionOrdersOpen,
                documentationOrdersOpen,
                totalMaintenanceCostLast30Days,
                totalMaintenanceCostAllTime,
                latestOrders
        );
    }

    public VehicleMaintenanceOrderResponse toOrderResponse(VehicleMaintenanceRecord record, Vehicle vehicle) {
        return toOrderResponse(record, vehicle, OffsetDateTime.now(ZoneOffset.UTC));
    }

    private VehicleMaintenanceOrderResponse toOrderResponse(VehicleMaintenanceRecord record, Vehicle vehicle, OffsetDateTime referenceTime) {
        // Traduz a entidade persistida para uma visao de ordem de servico com contexto operacional.
        Long kmRemainingAtService = record.getKmAtService() != null && record.getNextMaintenanceKm() != null
                ? record.getNextMaintenanceKm() - record.getKmAtService()
                : null;
        Long daysUntilDue = record.getDueDate() == null
                ? null
                : java.time.temporal.ChronoUnit.DAYS.between(referenceTime.toLocalDate(), record.getDueDate());
        long ageDays = record.getOpenedAt() == null ? 0 : java.time.Duration.between(record.getOpenedAt(), referenceTime).toDays();

        return new VehicleMaintenanceOrderResponse(
                record.getId(),
                record.getWorkOrderCode(),
                record.getVehicleId(),
                record.getVehiclePlate(),
                vehicle != null ? vehicle.getModel() : "Veiculo nao encontrado",
                record.getType(),
                record.getPriority(),
                record.getStatus(),
                record.getLifecycleStatus(),
                record.getLifecycleLabel(),
                record.getOpenedAt(),
                record.getCompletedAt(),
                record.getServiceDate(),
                record.getDueDate(),
                record.getKmAtService(),
                record.getNextMaintenanceKm(),
                kmRemainingAtService,
                daysUntilDue,
                record.getCostAmount(),
                record.getSupplierName(),
                record.getResolutionNotes(),
                record.getDescription(),
                record.isResolved(),
                record.isBlockingOrder(),
                ageDays
        );
    }
}
