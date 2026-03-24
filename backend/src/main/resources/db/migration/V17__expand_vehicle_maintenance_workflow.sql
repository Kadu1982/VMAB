-- Aprofunda a ordem de servico da frota com prioridade, status e prazo operacional.
ALTER TABLE vehicle_maintenance_records
    ADD COLUMN maintenance_code VARCHAR(40),
    ADD COLUMN priority VARCHAR(20),
    ADD COLUMN status VARCHAR(30),
    ADD COLUMN due_date DATE,
    ADD COLUMN resolution_notes VARCHAR(1000);

UPDATE vehicle_maintenance_records
SET maintenance_code = CONCAT('OS-', LPAD(id::text, 6, '0')),
    priority = CASE
        WHEN type = 'CORRECTIVE' THEN 'HIGH'
        WHEN type = 'DOCUMENTATION' THEN 'MEDIUM'
        ELSE 'LOW'
    END,
    status = CASE
        WHEN resolved THEN 'COMPLETED'
        ELSE 'OPEN'
    END;

ALTER TABLE vehicle_maintenance_records
    ALTER COLUMN maintenance_code SET NOT NULL,
    ALTER COLUMN priority SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE vehicle_maintenance_records
    ADD CONSTRAINT uk_vehicle_maintenance_code UNIQUE (maintenance_code);
