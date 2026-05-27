package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.FleetOperationalReportResponse;
import com.seguranca.plataforma.operations.dto.CreateVehicleMaintenanceRequest;
import com.seguranca.plataforma.operations.dto.CreateVehicleRequest;
import com.seguranca.plataforma.operations.dto.UpdateVehicleMaintenanceRequest;
import com.seguranca.plataforma.operations.dto.UpdateVehicleRequest;
import com.seguranca.plataforma.operations.dto.VehicleMaintenanceOrderResponse;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import com.seguranca.plataforma.operations.service.OperationsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    // CRUD da frota operacional.

    private final OperationsService operationsService;

    public VehicleController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping
    public List<Vehicle> list() {
        return operationsService.listVehicles();
    }

    @GetMapping("/maintenance/orders")
    public List<VehicleMaintenanceOrderResponse> maintenanceOrders() {
        // Entrega a frota em formato de ordem de servico para leitura operacional.
        return operationsService.listVehicleMaintenanceOrders();
    }

    @GetMapping("/report")
    public FleetOperationalReportResponse report() {
        // Consolida os indicadores da frota sem depender do resumo geral do dashboard.
        return operationsService.fleetReport();
    }

    @PostMapping
    public Vehicle create(@Valid @RequestBody CreateVehicleRequest request) {
        return operationsService.addVehicle(request);
    }

    @PutMapping("/{id}")
    public Vehicle update(@PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest request) {
        return operationsService.updateVehicle(id, request);
    }

    @PostMapping("/{id}/maintenance")
    public VehicleMaintenanceRecord createMaintenance(@PathVariable Long id, @Valid @RequestBody CreateVehicleMaintenanceRequest request) {
        // Registra uma OS de manutenção vinculada a viatura.
        return operationsService.addVehicleMaintenance(id, request);
    }

    @PutMapping("/maintenance/{maintenanceId}")
    public VehicleMaintenanceRecord updateMaintenance(@PathVariable Long maintenanceId, @Valid @RequestBody UpdateVehicleMaintenanceRequest request) {
        // Atualiza o desfecho da manutenção sem apagar o historico anterior.
        return operationsService.updateVehicleMaintenance(maintenanceId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        operationsService.deleteVehicle(id);
    }
}


