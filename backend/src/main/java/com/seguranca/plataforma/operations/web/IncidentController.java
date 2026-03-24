package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.CloseIncidentRequest;
import com.seguranca.plataforma.operations.dto.CreateIncidentRequest;
import com.seguranca.plataforma.operations.dto.DispatchIncidentRequest;
import com.seguranca.plataforma.operations.dto.OnSiteIncidentRequest;
import com.seguranca.plataforma.operations.dto.RondaCloseIncidentRequest;
import com.seguranca.plataforma.operations.dto.RondaDispatchIncidentRequest;
import com.seguranca.plataforma.operations.dto.UpdateIncidentRequest;
import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.service.OperationsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    // Centraliza tanto o fluxo administrativo da central quanto o fluxo mobile da ronda sobre ocorrencias.

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
        // Mantem o despacho administrativo da central com agente e viatura escolhidos explicitamente.
        return operationsService.dispatchIncident(id, request);
    }

    @PostMapping("/{id}/dispatch/me")
    public Incident dispatchForCurrentRonda(@PathVariable Long id, @RequestBody(required = false) RondaDispatchIncidentRequest request) {
        // Permite que a ronda assuma a ocorrencia usando o proprio turno e a propria viatura vinculada.
        return operationsService.dispatchIncidentForCurrentRonda(id, request);
    }

    @PostMapping("/{id}/onsite")
    public Incident onsite(@PathVariable Long id, @Valid @RequestBody OnSiteIncidentRequest request) {
        // Mantem o registro administrativo de chegada quando a central estiver operando esse passo.
        return operationsService.markIncidentOnSite(id, request);
    }

    @PostMapping("/{id}/onsite/me")
    public Incident onsiteForCurrentRonda(@PathVariable Long id, @RequestBody(required = false) OnSiteIncidentRequest request) {
        // Permite que a ronda confirme a chegada apenas na ocorrencia atribuida a ela.
        return operationsService.markIncidentOnSiteForCurrentRonda(id, request);
    }

    @PostMapping("/{id}/close")
    public Incident close(@PathVariable Long id, @Valid @RequestBody CloseIncidentRequest request) {
        // Encerramento administrativo da ocorrencia para fluxos operados diretamente pela central.
        return operationsService.closeIncident(id, request);
    }

    @PostMapping("/{id}/close/me")
    public Incident closeForCurrentRonda(@PathVariable Long id, @Valid @RequestBody RondaCloseIncidentRequest request) {
        // Encerramento mobile da ronda com observacao final obrigatoria.
        return operationsService.closeIncidentForCurrentRonda(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        operationsService.deleteIncident(id);
    }
}
