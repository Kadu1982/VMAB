alter table shifts
    add column if not exists handoff_requested_at timestamptz,
    add column if not exists handoff_requested_by varchar(120),
    add column if not exists handoff_rejected_at timestamptz,
    add column if not exists handoff_rejected_by varchar(120),
    add column if not exists handoff_rejection_reason varchar(500);
