create table if not exists app_user_push_devices (
    id bigserial primary key,
    user_id bigint not null references app_users(id) on delete cascade,
    expo_push_token varchar(255) not null unique,
    device_label varchar(120),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    revoked_at timestamptz
);

create index if not exists idx_app_user_push_devices_user_id
    on app_user_push_devices (user_id);
