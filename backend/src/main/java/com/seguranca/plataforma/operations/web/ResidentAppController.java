package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.residentapp.dto.CreateResidentAlertRequest;
import com.seguranca.plataforma.operations.dto.ActivePatrolResponse;
import com.seguranca.plataforma.operations.residentapp.dto.RegisterResidentPushTokenRequest;
import com.seguranca.plataforma.operations.residentapp.dto.RevokeResidentPushTokenRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    @PostMapping("/session/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        residentAlertService.logout(authorization);
    }

    @PostMapping("/push-device")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerPushDevice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody RegisterResidentPushTokenRequest request
    ) {
        residentAlertService.registerPushDevice(authorization, request);
    }

    @PostMapping("/push-device/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokePushDevice(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody RevokeResidentPushTokenRequest request
    ) {
        residentAlertService.revokePushDevice(authorization, request);
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

    @GetMapping("/patrol")
    public ActivePatrolResponse patrol(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return residentAlertService.getVisiblePatrol(authorization);
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
