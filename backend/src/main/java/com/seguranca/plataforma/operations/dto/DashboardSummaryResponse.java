package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import java.util.List;

// Resposta agregada do painel administrativo.
public record DashboardSummaryResponse(
        long totalResidents,
        long totalAgents,
        long activeAgents,
        long availableVehicles,
        long activeShifts,
        long lateShifts,
        long absentShifts,
        long openIncidents,
        long maintenanceAlerts,
        ActivePatrolResponse activePatrol,
        List<Resident> residents,
        List<Agent> agents,
        List<Vehicle> vehicles,
        List<VehicleMaintenanceRecord> maintenanceRecords,
        List<Shift> shifts,
        List<Incident> incidents
) {
}
