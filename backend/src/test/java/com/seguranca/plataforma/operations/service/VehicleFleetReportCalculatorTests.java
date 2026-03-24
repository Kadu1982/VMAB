package com.seguranca.plataforma.operations.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleMaintenancePriority;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceStatus;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceType;
import com.seguranca.plataforma.operations.model.VehicleStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VehicleFleetReportCalculatorTests {

    @Test
    void deveMontarRelatorioDaFrotaComIndicadoresCoerentes() {
        // O calculo do relatorio fica isolado para garantir que os indicadores da frota nao dependam do dashboard.
        Vehicle availableVehicle = new Vehicle(
                "ABC1D23",
                "Renault Duster",
                10_000,
                11_200,
                VehicleStatus.AVAILABLE,
                LocalDate.now(ZoneOffset.UTC).plusDays(60),
                LocalDate.now(ZoneOffset.UTC).plusDays(60),
                LocalDate.now(ZoneOffset.UTC).plusDays(60),
                LocalDate.now(ZoneOffset.UTC).minusDays(4),
                "Viatura pronta"
        );
        ReflectionTestUtils.setField(availableVehicle, "id", 1L);

        Vehicle maintenanceVehicle = new Vehicle(
                "FGH4J56",
                "Chevrolet Spin",
                20_000,
                20_100,
                VehicleStatus.MAINTENANCE,
                LocalDate.now(ZoneOffset.UTC).plusDays(8),
                LocalDate.now(ZoneOffset.UTC).plusDays(10),
                LocalDate.now(ZoneOffset.UTC).plusDays(12),
                LocalDate.now(ZoneOffset.UTC).minusDays(2),
                "Aguardando liberacao"
        );
        ReflectionTestUtils.setField(maintenanceVehicle, "id", 2L);

        VehicleMaintenanceRecord resolvedOrder = new VehicleMaintenanceRecord(
                1L,
                "ABC1D23",
                "OS-ABC1D23-000010",
                VehicleMaintenanceType.PREVENTIVE,
                VehicleMaintenancePriority.LOW,
                VehicleMaintenanceStatus.COMPLETED,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(5),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(4),
                LocalDate.now(ZoneOffset.UTC).minusDays(4),
                LocalDate.now(ZoneOffset.UTC).plusMonths(6),
                10_000L,
                11_200L,
                new BigDecimal("450.00"),
                "Oficina Central",
                "Manutencao concluida sem ocorrencias.",
                "Troca de oleo e filtros.",
                true
        );
        ReflectionTestUtils.setField(resolvedOrder, "id", 10L);

        VehicleMaintenanceRecord openCorrectiveOrder = new VehicleMaintenanceRecord(
                2L,
                "FGH4J56",
                "OS-FGH4J56-000011",
                VehicleMaintenanceType.CORRECTIVE,
                VehicleMaintenancePriority.CRITICAL,
                VehicleMaintenanceStatus.IN_PROGRESS,
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(2),
                null,
                LocalDate.now(ZoneOffset.UTC).minusDays(1),
                LocalDate.now(ZoneOffset.UTC).plusDays(7),
                20_000L,
                20_100L,
                new BigDecimal("1200.00"),
                "Auto Center",
                null,
                "Ruido na suspensao dianteira.",
                false
        );
        ReflectionTestUtils.setField(openCorrectiveOrder, "id", 11L);

        VehicleFleetReportCalculator calculator = new VehicleFleetReportCalculator();
        var report = calculator.buildReport(
                List.of(availableVehicle, maintenanceVehicle),
                List.of(resolvedOrder, openCorrectiveOrder)
        );

        assertEquals(2, report.totalVehicles());
        assertEquals(1, report.operationalVehicles());
        assertEquals(1, report.availableVehicles());
        assertEquals(0, report.inOperationVehicles());
        assertEquals(1, report.maintenanceVehicles());
        assertEquals(1, report.maintenanceDueSoonVehicles());
        assertEquals(0, report.maintenanceOverdueVehicles());
        assertEquals(1, report.documentAlertVehicles());
        assertEquals(1, report.maintenanceOrdersOpen());
        assertEquals(1, report.maintenanceOrdersResolved());
        assertEquals(1, report.correctiveOrdersOpen());
        assertEquals(1, report.preventiveOrdersOpen());
        assertEquals(0, report.inspectionOrdersOpen());
        assertEquals(0, report.documentationOrdersOpen());
        assertEquals(2, report.latestOrders().size());
        assertEquals("OS-FGH4J56-000011", report.latestOrders().get(0).workOrderCode());
        assertEquals("Bloqueante", report.latestOrders().get(0).lifecycleLabel());
        assertTrue(report.latestOrders().get(0).blockingVehicle());
        assertFalse(report.latestOrders().get(1).blockingVehicle());
    }

    @Test
    void deveExporCodigoFormalDaOrdemMesmoAntesDaPersistencia() {
        // Em memoria, a OS ainda nao tem id; o codigo nao pode quebrar o relatorio por isso.
        VehicleMaintenanceRecord draftOrder = new VehicleMaintenanceRecord(
                3L,
                "JKL9M87",
                null,
                VehicleMaintenanceType.DOCUMENTATION,
                VehicleMaintenancePriority.MEDIUM,
                VehicleMaintenanceStatus.OPEN,
                OffsetDateTime.now(ZoneOffset.UTC),
                null,
                LocalDate.now(ZoneOffset.UTC),
                LocalDate.now(ZoneOffset.UTC).plusDays(30),
                25_000L,
                26_000L,
                new BigDecimal("300.00"),
                "Despachante",
                null,
                "Renovacao documental da viatura.",
                false
        );

        assertEquals("OS-RASCUNHO", draftOrder.getWorkOrderCode());
        assertEquals("Documentacao", draftOrder.getLifecycleLabel());
        assertFalse(draftOrder.isBlockingOrder());
    }
}
