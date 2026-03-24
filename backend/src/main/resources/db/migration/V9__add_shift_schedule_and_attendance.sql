-- Adiciona dados de escala, atraso e cobertura ao turno.
ALTER TABLE shifts
    ADD COLUMN scheduled_start_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN attendance_status VARCHAR(20),
    ADD COLUMN late_minutes INTEGER,
    ADD COLUMN coverage_for_agent_id BIGINT,
    ADD COLUMN coverage_for_agent_name VARCHAR(255),
    ADD COLUMN attendance_notes VARCHAR(500);

-- Backfill minimo para manter os turnos existentes consistentes.
UPDATE shifts
SET scheduled_start_at = COALESCE(started_at, scheduled_end_at - INTERVAL '8 hours');

UPDATE shifts
SET attendance_status = CASE
    WHEN status = 'PLANNED' THEN 'PENDING'
    WHEN check_in_at IS NULL THEN 'PENDING'
    WHEN check_in_at > scheduled_start_at THEN 'LATE'
    ELSE 'ON_TIME'
END;

UPDATE shifts
SET late_minutes = CASE
    WHEN check_in_at IS NULL OR scheduled_start_at IS NULL THEN NULL
    WHEN check_in_at > scheduled_start_at THEN FLOOR(EXTRACT(EPOCH FROM (check_in_at - scheduled_start_at)) / 60)
    ELSE 0
END;

ALTER TABLE shifts
    ALTER COLUMN scheduled_start_at SET NOT NULL,
    ALTER COLUMN attendance_status SET NOT NULL;
