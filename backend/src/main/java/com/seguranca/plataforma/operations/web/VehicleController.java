package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateVehicleRequest;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.service.OperationsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/vehicles")
public class VehicleController {

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
}
