package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateIncidentRequest;
import com.seguranca.plataforma.operations.model.Incident;
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
@RequestMapping("/api/incidents")
public class IncidentController {

    private final OperationsService operationsService;

    public IncidentController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping
    public List<Incident> list() {
        return operationsService.listIncidents();
    }

    @PostMapping
    public Incident create(@Valid @RequestBody CreateIncidentRequest request) {
        return operationsService.addIncident(request);
    }
}
