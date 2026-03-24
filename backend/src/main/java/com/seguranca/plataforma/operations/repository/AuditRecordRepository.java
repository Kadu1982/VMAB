package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.AuditRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
    List<AuditRecord> findTop10ByOrderByOccurredAtDesc();

    Optional<AuditRecord> findTopByEntityNameOrderByOccurredAtDesc(String entityName);
}
