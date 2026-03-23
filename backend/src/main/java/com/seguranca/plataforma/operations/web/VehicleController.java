package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateVehicleRequest;
import com.seguranca.plataforma.operations.dto.UpdateVehicleRequest;
import com.seguranca.plataforma.operations.model.Vehicle;
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

    @PostMapping
    public Vehicle create(@Valid @RequestBody CreateVehicleRequest request) {
        return operationsService.addVehicle(request);
    }

    @PutMapping("/{id}")
    public Vehicle update(@PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest request) {
        return operationsService.updateVehicle(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        operationsService.deleteVehicle(id);
    }
}
