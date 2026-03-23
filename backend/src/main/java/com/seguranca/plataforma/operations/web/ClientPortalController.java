package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.ClientPortalResponse;
import com.seguranca.plataforma.operations.service.OperationsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
public class ClientPortalController {
    // Exponibiliza a visao reduzida do cliente contratante.

    private final OperationsService operationsService;

    public ClientPortalController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping("/portal")
    public ClientPortalResponse portal() {
        return operationsService.clientPortal();
    }
}
