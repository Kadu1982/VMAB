package com.seguranca.plataforma.operations.dto;

import java.time.OffsetDateTime;

public record OperationsStreamEventResponse(
        String type,
        String entityName,
        Long entityId,
        String description,
        OffsetDateTime occurredAt
) {
}


