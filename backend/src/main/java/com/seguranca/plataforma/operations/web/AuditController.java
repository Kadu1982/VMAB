package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.AuditReportCategory;
import com.seguranca.plataforma.operations.dto.AuditReportResponse;
import com.seguranca.plataforma.operations.service.AuditReportService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditReportService auditReportService;

    public AuditController(AuditReportService auditReportService) {
        this.auditReportService = auditReportService;
    }

    @GetMapping("/report")
    public AuditReportResponse report(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "ALL") AuditReportCategory category,
            @RequestParam(defaultValue = "false") boolean includeAuth
    ) {
        return auditReportService.buildReport(days, category, includeAuth);
    }
}
