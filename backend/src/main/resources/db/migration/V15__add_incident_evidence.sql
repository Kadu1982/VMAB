create table if not exists incident_evidence (
    id bigserial primary key,
    incident_id bigint not null references incidents(id) on delete cascade,
    original_filename varchar(255) not null,
    stored_filename varchar(255) not null unique,
    content_type varchar(120),
    file_size_bytes bigint not null,
    notes varchar(1000),
    uploaded_by varchar(120) not null,
    uploaded_at timestamptz not null
);
