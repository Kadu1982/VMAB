ALTER TABLE privacy_requests
    ADD COLUMN IF NOT EXISTS subject_notified_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS subject_notification_channel VARCHAR(80),
    ADD COLUMN IF NOT EXISTS subject_notification_notes VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS export_generated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS deletion_applied_at TIMESTAMP WITH TIME ZONE;
