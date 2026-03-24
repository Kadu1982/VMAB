package com.seguranca.plataforma.privacy.web;

import com.seguranca.plataforma.privacy.dto.CreatePrivacyRequestRequest;
import com.seguranca.plataforma.privacy.dto.PrivacyExportResponse;
import com.seguranca.plataforma.privacy.dto.PrivacyRequestResponse;
import com.seguranca.plataforma.privacy.dto.UpdatePrivacyRequestRequest;
import com.seguranca.plataforma.privacy.model.PrivacySubjectType;
import com.seguranca.plataforma.privacy.service.PrivacyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/privacy")
public class PrivacyController {
    // Exponibiliza pedidos LGPD e exportacao controlada de dados pessoais.

    private final PrivacyService privacyService;

    public PrivacyController(PrivacyService privacyService) {
        this.privacyService = privacyService;
    }

    @GetMapping("/requests")
    public List<PrivacyRequestResponse> listRequests() {
        return privacyService.listRequests();
    }

    @PostMapping("/requests")
    public PrivacyRequestResponse createRequest(@Valid @RequestBody CreatePrivacyRequestRequest request) {
        return privacyService.createRequest(request);
    }

    @PutMapping("/requests/{id}")
    public PrivacyRequestResponse updateRequest(@PathVariable Long id, @Valid @RequestBody UpdatePrivacyRequestRequest request) {
        return privacyService.updateRequest(id, request);
    }

    @GetMapping("/exports/{subjectType}/{subjectId}")
    public PrivacyExportResponse exportSubject(@PathVariable PrivacySubjectType subjectType, @PathVariable Long subjectId) {
        return privacyService.exportSubject(subjectType, subjectId);
    }
}
