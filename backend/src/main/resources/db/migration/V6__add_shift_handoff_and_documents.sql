alter table agents
    add column medical_exam_expiry date,
    add column work_exams_expiry date,
    add column document_notes varchar(500);

alter table vehicles
    add column ipva_expiry date,
    add column licensing_expiry date,
    add column insurance_expiry date,
    add column last_maintenance_at date,
    add column maintenance_notes varchar(500);

alter table shifts
    add column check_in_at timestamptz,
    add column check_out_at timestamptz,
    add column start_km bigint,
    add column end_km bigint,
    add column handoff_from_agent_id bigint,
    add column handoff_from_agent_name varchar(255),
    add column handoff_to_agent_id bigint,
    add column handoff_to_agent_name varchar(255),
    add column handoff_accepted_at timestamptz,
    add column handoff_notes varchar(500);

update shifts
set check_in_at = started_at
where check_in_at is null;
