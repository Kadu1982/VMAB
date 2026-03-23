package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.DashboardSummaryResponse;
import com.seguranca.plataforma.operations.service.OperationsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final OperationsService operationsService;

    public DashboardController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return operationsService.summary();
    }
}
