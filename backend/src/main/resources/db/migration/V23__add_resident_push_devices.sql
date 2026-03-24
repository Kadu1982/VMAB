CREATE TABLE IF NOT EXISTS resident_push_devices (
    id BIGSERIAL PRIMARY KEY,
    resident_id BIGINT NOT NULL REFERENCES residents(id) ON DELETE CASCADE,
    expo_push_token VARCHAR(255) NOT NULL UNIQUE,
    device_label VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_resident_push_devices_resident_active
    ON resident_push_devices (resident_id, revoked_at);
