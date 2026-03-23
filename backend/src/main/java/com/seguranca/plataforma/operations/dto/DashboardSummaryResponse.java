package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.model.Vehicle;
import java.util.List;

public record DashboardSummaryResponse(
        long totalAgents,
        long activeAgents,
        long availableVehicles,
        long activeShifts,
        long openIncidents,
        long maintenanceAlerts,
        List<Agent> agents,
        List<Vehicle> vehicles,
        List<Shift> shifts,
        List<Incident> incidents
) {
}
