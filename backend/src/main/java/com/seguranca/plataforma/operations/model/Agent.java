package com.seguranca.plataforma.operations.model;

import java.time.LocalDate;

public record Agent(
        Long id,
        String fullName,
        String badgeCode,
        String cnhCategory,
        LocalDate cnhExpiry,
        AgentStatus status,
        String photoUrl
) {
}
