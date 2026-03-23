create table app_users (
    id bigserial primary key,
    username varchar(120) not null unique,
    password_hash varchar(255) not null,
    role varchar(40) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now()
);

insert into app_users (username, password_hash, role, enabled)
values
    ('admin', '{noop}admin123', 'ADMIN', true),
    ('supervisor', '{noop}super123', 'SUPERVISOR', true),
    ('cliente', '{noop}cliente123', 'CLIENT', true),
    ('ronda', '{noop}ronda123', 'RONDA', true);
