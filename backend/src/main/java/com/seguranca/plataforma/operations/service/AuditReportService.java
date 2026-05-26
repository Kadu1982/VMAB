package com.seguranca.plataforma.operations.service;

import com.seguranca.plataforma.operations.dto.AuditReportCategory;
import com.seguranca.plataforma.operations.dto.AuditReportResponse;
import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AuditReportService {

    private final AuditRecordRepository auditRecordRepository;

    public AuditReportService(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    public AuditReportResponse buildReport(int days, AuditReportCategory category, boolean includeAuth) {
        int safeDays = Math.max(days, 1);
        OffsetDateTime to = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime from = to.minusDays(safeDays);

        List<AuditRecord> baseRecords = auditRecordRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(from, to).stream()
                .filter(record -> includeAuth || record.getActionType() != AuditActionType.AUTH)
                .sorted(byMostRecentFirst())
                .toList();

        List<AuditRecord> visibleRecords = baseRecords.stream()
                .filter(record -> category == AuditReportCategory.ALL || classifyRecord(record) == category)
                .toList();

        return new AuditReportResponse(
                to,
                from,
                to,
                safeDays,
                category,
                includeAuth,
                baseRecords.size(),
                visibleRecords.size(),
                countByCategory(baseRecords),
                countByActionType(baseRecords),
                countByActor(baseRecords),
                visibleRecords
        );
    }

    private Comparator<AuditRecord> byMostRecentFirst() {
        return Comparator.comparing(AuditRecord::getOccurredAt).reversed()
                .thenComparing(AuditRecord::getId, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private Map<String, Long> countByCategory(List<AuditRecord> records) {
        LinkedHashMap<String, Long> counters = new LinkedHashMap<>();
        for (AuditReportCategory category : Arrays.stream(AuditReportCategory.values())
                .filter(value -> value != AuditReportCategory.ALL)
                .toList()) {
            counters.put(category.name(), 0L);
        }

        records.forEach(record -> {
            String key = classifyRecord(record).name();
            counters.put(key, counters.getOrDefault(key, 0L) + 1L);
        });
        return counters;
    }

    private Map<String, Long> countByActionType(List<AuditRecord> records) {
        return sortByCount(records.stream()
                .collect(Collectors.groupingBy(record -> record.getActionType().name(), Collectors.counting())));
    }

    private Map<String, Long> countByActor(List<AuditRecord> records) {
        return sortByCount(records.stream()
                .collect(Collectors.groupingBy(AuditRecord::getActorUsername, Collectors.counting())));
    }

    private Map<String, Long> sortByCount(Map<String, Long> counts) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private AuditReportCategory classifyRecord(AuditRecord record) {
        String entityName = record.getEntityName().toLowerCase();

        if (record.getActionType() == AuditActionType.AUTH || record.getActionType() == AuditActionType.RESIDENT_ALERT) {
            return AuditReportCategory.SEGURANCA;
        }

        if (record.getActionType() == AuditActionType.MAINTENANCE || entityName.contains("vehiclemaintenance") || entityName.contains("vehicle")) {
            return AuditReportCategory.FROTA;
        }

        if (record.getActionType() == AuditActionType.HANDOFF
                || record.getActionType() == AuditActionType.INCIDENT_WORKFLOW
                || entityName.contains("incident")
                || entityName.contains("residentalert")) {
            return AuditReportCategory.OPERACIONAL;
        }

        if (entityName.contains("hr") || entityName.contains("agent")) {
            return AuditReportCategory.RH;
        }

        return AuditReportCategory.GESTAO;
    }
}
