package com.seguranca.plataforma.operations.dto;

import com.seguranca.plataforma.operations.model.Incident;
import java.util.List;

public record ClientPortalResponse(
        long activeShifts,
        long openIncidents,
        long availableVehicles,
        long maintenanceAlerts,
        List<Incident> recentIncidents
) {
}
