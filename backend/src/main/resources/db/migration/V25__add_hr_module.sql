create table if not exists hr_employees (
    id bigserial primary key,
    employee_code varchar(64) not null unique,
    full_name varchar(180) not null,
    category varchar(40) not null,
    status varchar(40) not null,
    document_number varchar(60),
    phone_number varchar(30),
    email varchar(180),
    photo_url varchar(500),
    cnh_category varchar(20),
    cnh_expiry date,
    medical_exam_expiry date,
    training_expiry date,
    training_notes varchar(500),
    document_notes varchar(500),
    hire_date date,
    termination_date date,
    linked_agent_id bigint unique references agents(id) on delete set null,
    linked_app_user_id bigint unique references app_users(id) on delete set null,
    point_enabled boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    last_check_in_at timestamptz,
    last_check_out_at timestamptz,
    last_check_in_device varchar(120),
    last_check_out_device varchar(120),
    last_check_in_latitude double precision,
    last_check_in_longitude double precision,
    last_check_out_latitude double precision,
    last_check_out_longitude double precision,
    last_point_notes varchar(500)
);

create index if not exists idx_hr_employees_status
    on hr_employees (status);

create index if not exists idx_hr_employees_category
    on hr_employees (category);

create index if not exists idx_hr_employees_linked_agent_id
    on hr_employees (linked_agent_id);

create index if not exists idx_hr_employees_linked_app_user_id
    on hr_employees (linked_app_user_id);

create table if not exists hr_attendance_records (
    id bigserial primary key,
    employee_id bigint not null references hr_employees(id) on delete cascade,
    event_type varchar(20) not null,
    occurred_at timestamptz not null,
    device_label varchar(120),
    latitude double precision,
    longitude double precision,
    note varchar(500),
    anomaly_flag boolean not null default false,
    anomaly_reason varchar(255)
);

create index if not exists idx_hr_attendance_records_employee_id
    on hr_attendance_records (employee_id);

create index if not exists idx_hr_attendance_records_occurred_at
    on hr_attendance_records (occurred_at desc);
