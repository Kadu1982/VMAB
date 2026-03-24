package com.seguranca.plataforma.operations.web;

import com.seguranca.plataforma.operations.dto.ClientPortalResponse;
import com.seguranca.plataforma.operations.dto.ClientOperationalReportResponse;
import com.seguranca.plataforma.operations.service.OperationsService;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/report")
    public ClientOperationalReportResponse report() {
        return operationsService.clientOperationalReport();
    }

    @GetMapping("/report/export.csv")
    public ResponseEntity<ByteArrayResource> exportReportCsv() {
        ClientOperationalReportResponse report = operationsService.clientOperationalReport();
        StringBuilder csv = new StringBuilder();
        csv.append("gerado_em,turnos_ativos,ocorrencias_abertas,viaturas_disponiveis,alertas_manutencao,os_abertas,os_criticas,turnos_atrasados,faltas,media_despacho_min,media_resolucao_min\n");
        csv.append(report.generatedAt()).append(',')
                .append(report.activeShifts()).append(',')
                .append(report.openIncidents()).append(',')
                .append(report.availableVehicles()).append(',')
                .append(report.maintenanceAlerts()).append(',')
                .append(report.openMaintenanceOrders()).append(',')
                .append(report.criticalMaintenanceOrders()).append(',')
                .append(report.lateShifts()).append(',')
                .append(report.absentShifts()).append(',')
                .append(String.format(java.util.Locale.US, "%.2f", report.averageDispatchMinutes())).append(',')
                .append(String.format(java.util.Locale.US, "%.2f", report.averageResolutionMinutes())).append('\n');

        csv.append('\n');
        csv.append("incidente_id,tipo,prioridade,status,morador,endereco,agente,viatura,aberta_em,despachada_em,encerrada_em\n");
        report.incidents().forEach(incident -> csv
                .append(incident.getId()).append(',')
                .append(incident.getType()).append(',')
                .append(incident.getPriority()).append(',')
                .append(incident.getStatus()).append(',')
                .append(escapeCsv(incident.getResidentName())).append(',')
                .append(escapeCsv(incident.getAddress())).append(',')
                .append(escapeCsv(incident.getAssignedAgentName())).append(',')
                .append(escapeCsv(incident.getVehiclePlate())).append(',')
                .append(incident.getOpenedAt()).append(',')
                .append(incident.getDispatchedAt()).append(',')
                .append(incident.getClosedAt()).append('\n'));

        csv.append('\n');
        csv.append("os_id,codigo,viatura,tipo,prioridade,status,ciclo,custo,fornecedor,descricao\n");
        report.maintenanceOrders().forEach(order -> csv
                .append(order.id()).append(',')
                .append(escapeCsv(order.workOrderCode())).append(',')
                .append(escapeCsv(order.vehiclePlate())).append(',')
                .append(order.type()).append(',')
                .append(order.priority()).append(',')
                .append(order.status()).append(',')
                .append(order.lifecycleStatus()).append(',')
                .append(order.costAmount()).append(',')
                .append(escapeCsv(order.supplierName())).append(',')
                .append(escapeCsv(order.description())).append('\n'));

        byte[] payload = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vmab-relatorio-cliente.csv")
                .contentType(new MediaType("text", "csv"))
                .contentLength(payload.length)
                .body(new ByteArrayResource(payload));
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
