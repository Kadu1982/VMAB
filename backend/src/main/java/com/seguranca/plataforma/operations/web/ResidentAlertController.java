package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.residentapp.dto.DispatchResidentAlertRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentAlertActionNotesRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentAlertResponse;
import com.seguranca.plataforma.operations.residentapp.service.ResidentAlertService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resident-alerts")
public class ResidentAlertController {
    // Endpoints administrativos para recepcao, despacho e fechamento do alerta.

    private final ResidentAlertService residentAlertService;

    public ResidentAlertController(ResidentAlertService residentAlertService) {
        this.residentAlertService = residentAlertService;
    }

    @GetMapping
    public List<ResidentAlertResponse> list() {
        return residentAlertService.listAllAlerts();
    }

    @PostMapping("/{id}/acknowledge")
    public ResidentAlertResponse acknowledge(@PathVariable Long id, @RequestBody(required = false) ResidentAlertActionNotesRequest request) {
        return residentAlertService.acknowledge(id, request);
    }

    @PostMapping("/{id}/dispatch")
    public ResidentAlertResponse dispatch(@PathVariable Long id, @Valid @RequestBody DispatchResidentAlertRequest request) {
        return residentAlertService.dispatch(id, request);
    }

    @PostMapping("/{id}/onsite")
    public ResidentAlertResponse onsite(@PathVariable Long id, @RequestBody(required = false) ResidentAlertActionNotesRequest request) {
        return residentAlertService.markOnSite(id, request);
    }

    @PostMapping("/{id}/resolve")
    public ResidentAlertResponse resolve(@PathVariable Long id, @RequestBody(required = false) ResidentAlertActionNotesRequest request) {
        return residentAlertService.resolve(id, request);
    }
}


