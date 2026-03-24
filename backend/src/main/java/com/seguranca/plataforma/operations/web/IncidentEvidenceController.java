package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.DeleteIncidentEvidenceRequest;
import com.seguranca.plataforma.operations.dto.IncidentEvidenceResponse;
import com.seguranca.plataforma.operations.service.OperationsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/incidents")
public class IncidentEvidenceController {
    // Exponibiliza upload e leitura de evidencias ligadas a ocorrencias.

    private final OperationsService operationsService;

    public IncidentEvidenceController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping("/evidence")
    public List<IncidentEvidenceResponse> listAll() {
        return operationsService.listIncidentEvidence();
    }

    @GetMapping("/{incidentId}/evidence")
    public List<IncidentEvidenceResponse> listByIncident(@PathVariable Long incidentId) {
        return operationsService.listIncidentEvidenceByIncident(incidentId);
    }

    @PostMapping(path = "/{incidentId}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public IncidentEvidenceResponse upload(
            @PathVariable Long incidentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes
    ) {
        return operationsService.addIncidentEvidence(incidentId, file, notes);
    }

    @GetMapping("/{incidentId}/evidence/{evidenceId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long incidentId,
            @PathVariable Long evidenceId
    ) {
        OperationsService.IncidentEvidenceDownload download = operationsService.downloadIncidentEvidence(incidentId, evidenceId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.originalFilename() + "\"")
                .body(download.resource());
    }

    @DeleteMapping("/{incidentId}/evidence/{evidenceId}")
    public IncidentEvidenceResponse delete(
            @PathVariable Long incidentId,
            @PathVariable Long evidenceId,
            @Valid @RequestBody(required = false) DeleteIncidentEvidenceRequest request
    ) {
        // Remove a evidencia de forma controlada e auditavel.
        return operationsService.deleteIncidentEvidence(incidentId, evidenceId, request == null ? null : request.reason());
    }
}
