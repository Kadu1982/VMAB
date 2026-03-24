create table privacy_requests (
    id bigserial primary key,
    request_type varchar(40) not null,
    subject_type varchar(40) not null,
    subject_id bigint not null,
    subject_label varchar(255) not null,
    status varchar(40) not null,
    requested_by varchar(120) not null,
    requested_at timestamptz not null,
    handled_by varchar(120),
    handled_at timestamptz,
    notes varchar(1000)
);

create index idx_privacy_requests_requested_at on privacy_requests(requested_at desc);
create index idx_privacy_requests_subject on privacy_requests(subject_type, subject_id);
