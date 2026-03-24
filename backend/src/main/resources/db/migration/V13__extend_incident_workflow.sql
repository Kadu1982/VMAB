-- Enriquece a ocorrencia com trilha operacional de despacho, chegada e fechamento.
ALTER TABLE incidents
    ADD COLUMN assigned_agent_id BIGINT,
    ADD COLUMN vehicle_id BIGINT,
    ADD COLUMN dispatched_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN on_site_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN closed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN dispatch_notes VARCHAR(500),
    ADD COLUMN arrival_notes VARCHAR(500),
    ADD COLUMN closure_notes VARCHAR(1000);
