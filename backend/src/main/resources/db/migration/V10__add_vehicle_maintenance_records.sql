-- Cria historico auditavel de manutencao da frota.
CREATE TABLE vehicle_maintenance_records (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    vehicle_plate VARCHAR(20) NOT NULL,
    type VARCHAR(30) NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    service_date DATE,
    km_at_service BIGINT,
    next_maintenance_km BIGINT,
    cost_amount NUMERIC(12, 2),
    supplier_name VARCHAR(255),
    description VARCHAR(1000) NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_vehicle_maintenance_vehicle_id ON vehicle_maintenance_records(vehicle_id);
