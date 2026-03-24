package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.residentapp.dto.CreateResidentAlertRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentAlertCancelRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentAlertResponse;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentLoginRequest;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentProfileResponse;
import com.seguranca.plataforma.operations.residentapp.dto.ResidentSessionResponse;
import com.seguranca.plataforma.operations.residentapp.service.ResidentAlertService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resident-app")
public class ResidentAppController {
    // API publica do morador para login, alerta e acompanhamento de status.

    private final ResidentAlertService residentAlertService;

    public ResidentAppController(ResidentAlertService residentAlertService) {
        this.residentAlertService = residentAlertService;
    }

    @PostMapping("/session")
    public ResidentSessionResponse login(@Valid @RequestBody ResidentLoginRequest request) {
        return residentAlertService.createSession(request);
    }

    @GetMapping("/me")
    public ResidentProfileResponse me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return residentAlertService.getProfile(authorization);
    }

    @GetMapping("/alerts")
    public List<ResidentAlertResponse> listAlerts(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return residentAlertService.listResidentAlerts(authorization);
    }

    @GetMapping("/alerts/{id}")
    public ResidentAlertResponse getAlert(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id
    ) {
        return residentAlertService.getResidentAlert(authorization, id);
    }

    @PostMapping("/alerts")
    public ResidentAlertResponse createAlert(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CreateResidentAlertRequest request
    ) {
        return residentAlertService.createAlert(authorization, request);
    }

    @PostMapping("/alerts/{id}/cancel")
    public ResidentAlertResponse cancelAlert(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id,
            @RequestBody(required = false) ResidentAlertCancelRequest request
    ) {
        return residentAlertService.cancelAlert(authorization, id, request);
    }
}
