-- Corrige a modelagem de jornada para permitir turno planejado sem inicio real.
ALTER TABLE shifts
    ALTER COLUMN started_at DROP NOT NULL;

-- Limpa ponto e KM inicial de turnos ainda planejados que foram gravados incorretamente como iniciados.
UPDATE shifts
SET started_at = NULL,
    check_in_at = NULL,
    start_km = NULL
WHERE status = 'PLANNED';
