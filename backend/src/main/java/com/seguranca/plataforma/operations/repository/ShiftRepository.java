package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.model.ShiftStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Optional<Shift> findFirstByAgentIdAndStatusInOrderByStartedAtDesc(Long agentId, List<ShiftStatus> statuses);
}


