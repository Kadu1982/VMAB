create table resident_sessions (
    id bigserial primary key,
    resident_id bigint not null references residents(id) on delete cascade,
    token_hash varchar(128) not null unique,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    last_seen_at timestamptz not null,
    revoked_at timestamptz
);

create index idx_resident_sessions_resident_id on resident_sessions(resident_id);

create table resident_alerts (
    id bigserial primary key,
    resident_id bigint not null references residents(id) on delete cascade,
    resident_name varchar(255) not null,
    resident_phone_number varchar(80) not null,
    resident_address varchar(255) not null,
    type varchar(40) not null,
    status varchar(40) not null,
    opened_at timestamptz not null,
    updated_at timestamptz not null,
    acknowledged_at timestamptz,
    dispatched_at timestamptz,
    on_site_at timestamptz,
    resolved_at timestamptz,
    cancelled_at timestamptz,
    latitude double precision,
    longitude double precision,
    notes varchar(1000),
    acknowledgment_notes varchar(500),
    dispatch_notes varchar(500),
    arrival_notes varchar(500),
    resolution_notes varchar(1000),
    cancellation_reason varchar(500),
    assigned_agent_id bigint,
    assigned_agent_name varchar(255),
    vehicle_id bigint,
    vehicle_plate varchar(40)
);

create index idx_resident_alerts_resident_id_opened_at on resident_alerts(resident_id, opened_at desc);
