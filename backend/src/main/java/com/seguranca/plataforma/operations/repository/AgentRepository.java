package com.seguranca.plataforma.operations.repository;

import com.seguranca.plataforma.operations.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
}


