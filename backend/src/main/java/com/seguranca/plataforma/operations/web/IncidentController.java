package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CreateIncidentRequest;
import com.seguranca.plataforma.operations.dto.CloseIncidentRequest;
import com.seguranca.plataforma.operations.dto.DispatchIncidentRequest;
import com.seguranca.plataforma.operations.dto.OnSiteIncidentRequest;
import com.seguranca.plataforma.operations.dto.UpdateIncidentRequest;
import com.seguranca.plataforma.operations.model.Incident;
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
@RequestMapping("/api/incidents")
public class IncidentController {
    // CRUD da central de ocorrencias.

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

    @PutMapping("/{id}")
    public Incident update(@PathVariable Long id, @Valid @RequestBody UpdateIncidentRequest request) {
        return operationsService.updateIncident(id, request);
    }

    @PostMapping("/{id}/dispatch")
    public Incident dispatch(@PathVariable Long id, @Valid @RequestBody DispatchIncidentRequest request) {
        // Expõe o despacho formal da ocorrencia para a base operacional.
        return operationsService.dispatchIncident(id, request);
    }

    @PostMapping("/{id}/onsite")
    public Incident onsite(@PathVariable Long id, @Valid @RequestBody OnSiteIncidentRequest request) {
        // Registra a chegada da equipe no local sem sobrescrever o historico anterior.
        return operationsService.markIncidentOnSite(id, request);
    }

    @PostMapping("/{id}/close")
    public Incident close(@PathVariable Long id, @Valid @RequestBody CloseIncidentRequest request) {
        // Formaliza o encerramento da ocorrencia com trilha temporal.
        return operationsService.closeIncident(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        operationsService.deleteIncident(id);
    }
}
