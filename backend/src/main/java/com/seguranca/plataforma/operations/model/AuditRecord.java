package com.seguranca.plataforma.operations.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_records")
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AuditActionType actionType;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "actor_username", nullable = false)
    private String actorUsername;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(length = 1000, nullable = false)
    private String description;

    protected AuditRecord() {
    }

    public AuditRecord(
            AuditActionType actionType,
            String entityName,
            Long entityId,
            String actorUsername,
            OffsetDateTime occurredAt,
            String description
    ) {
        this.actionType = actionType;
        this.entityName = entityName;
        this.entityId = entityId;
        this.actorUsername = actorUsername;
        this.occurredAt = occurredAt;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public AuditActionType getActionType() {
        return actionType;
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDescription() {
        return description;
    }
}
