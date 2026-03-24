-- Cria trilha de auditoria minima para acoes criticas.
CREATE TABLE audit_records (
    id BIGSERIAL PRIMARY KEY,
    action_type VARCHAR(30) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    actor_username VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    description VARCHAR(1000) NOT NULL
);

CREATE INDEX idx_audit_records_occurred_at ON audit_records(occurred_at DESC);
