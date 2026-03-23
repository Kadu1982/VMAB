package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.UpsertShiftTelemetryRequest;
import com.seguranca.plataforma.operations.model.ShiftTelemetry;
import com.seguranca.plataforma.operations.service.OperationsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shifts/{shiftId}/telemetry")
public class ShiftTelemetryController {
    // Recebe pontos de GPS do mobile e devolve o ultimo ponto conhecido do turno.

    private final OperationsService operationsService;

    public ShiftTelemetryController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @PostMapping
    public ShiftTelemetry upsert(@PathVariable Long shiftId, @Valid @RequestBody UpsertShiftTelemetryRequest request) {
        return operationsService.upsertShiftTelemetry(shiftId, request);
    }

    @GetMapping
    public ShiftTelemetry get(@PathVariable Long shiftId) {
        return operationsService.getShiftTelemetry(shiftId);
    }
}
