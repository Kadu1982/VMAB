package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.AuditRecord;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record AuditReportResponse(
        OffsetDateTime generatedAt,
        OffsetDateTime from,
        OffsetDateTime to,
        int days,
        AuditReportCategory category,
        boolean includeAuth,
        long totalRecords,
        long visibleRecords,
        Map<String, Long> recordsByCategory,
        Map<String, Long> recordsByActionType,
        Map<String, Long> recordsByActor,
        List<AuditRecord> records
) {
}


