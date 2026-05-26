alter table residents
    add column if not exists cpf varchar(60),
    add column if not exists photo_url varchar(500),
    add column if not exists business_unit_id bigint;

alter table hr_employees
    add column if not exists address varchar(255);

create table if not exists business_units (
    id bigserial primary key,
    name varchar(180) not null,
    type varchar(40) not null,
    cnpj varchar(60),
    active boolean not null default true,
    notes varchar(1000),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index if not exists idx_business_units_type
    on business_units (type);

create index if not exists idx_business_units_active
    on business_units (active);

create table if not exists business_sectors (
    id bigserial primary key,
    business_unit_id bigint not null references business_units(id) on delete cascade,
    name varchar(180) not null,
    code varchar(60),
    active boolean not null default true,
    notes varchar(1000),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_business_sectors_unit_name unique (business_unit_id, name)
);

create index if not exists idx_business_sectors_business_unit_id
    on business_sectors (business_unit_id);

create index if not exists idx_business_sectors_active
    on business_sectors (active);

create table if not exists resident_dependents (
    id bigserial primary key,
    resident_id bigint not null references residents(id) on delete cascade,
    full_name varchar(180) not null,
    cpf varchar(60),
    phone_number varchar(40),
    relationship varchar(40) not null,
    access_enabled boolean not null default false,
    app_enabled boolean not null default false,
    notes varchar(1000),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index if not exists idx_resident_dependents_resident_id
    on resident_dependents (resident_id);

create table if not exists resident_vehicles (
    id bigserial primary key,
    resident_id bigint not null references residents(id) on delete cascade,
    plate varchar(20) not null,
    model varchar(120),
    color varchar(60),
    active boolean not null default true,
    notes varchar(1000),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index if not exists idx_resident_vehicles_resident_id
    on resident_vehicles (resident_id);

create table if not exists hr_employee_assignments (
    id bigserial primary key,
    employee_id bigint not null references hr_employees(id) on delete cascade,
    business_unit_id bigint not null references business_units(id) on delete cascade,
    business_sector_id bigint references business_sectors(id) on delete set null,
    role_title varchar(120),
    start_date date,
    end_date date,
    active boolean not null default true,
    notes varchar(1000),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index if not exists idx_hr_employee_assignments_employee_id
    on hr_employee_assignments (employee_id);

create index if not exists idx_hr_employee_assignments_business_unit_id
    on hr_employee_assignments (business_unit_id);

create index if not exists idx_hr_employee_assignments_business_sector_id
    on hr_employee_assignments (business_sector_id);

create table if not exists hr_employee_dependents (
    id bigserial primary key,
    employee_id bigint not null references hr_employees(id) on delete cascade,
    full_name varchar(180) not null,
    cpf varchar(60),
    phone_number varchar(40),
    relationship varchar(40) not null,
    notes varchar(1000),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index if not exists idx_hr_employee_dependents_employee_id
    on hr_employee_dependents (employee_id);

create table if not exists person_documents (
    id bigserial primary key,
    owner_type varchar(40) not null,
    owner_id bigint not null,
    document_type varchar(60) not null,
    original_filename varchar(255) not null,
    stored_filename varchar(255) not null unique,
    content_type varchar(120),
    file_size_bytes bigint not null,
    notes varchar(1000),
    uploaded_by varchar(120) not null,
    uploaded_at timestamptz not null
);

create index if not exists idx_person_documents_owner
    on person_documents (owner_type, owner_id);

create index if not exists idx_person_documents_uploaded_at
    on person_documents (uploaded_at desc);

alter table residents
    add constraint fk_residents_business_unit
    foreign key (business_unit_id) references business_units(id) on delete set null;

create index if not exists idx_residents_business_unit_id
    on residents (business_unit_id);
