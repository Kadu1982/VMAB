alter table incident_evidence
    add column if not exists deleted_at timestamptz,
    add column if not exists deleted_by varchar(120),
    add column if not exists deletion_reason varchar(1000);
