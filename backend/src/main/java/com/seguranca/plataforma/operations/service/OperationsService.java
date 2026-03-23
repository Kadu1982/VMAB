package com.seguranca.plataforma.operations.service;

import com.seguranca.plataforma.operations.dto.CreateAgentRequest;
import com.seguranca.plataforma.operations.dto.CreateIncidentRequest;
import com.seguranca.plataforma.operations.dto.CreateShiftRequest;
import com.seguranca.plataforma.operations.dto.CreateVehicleRequest;
import com.seguranca.plataforma.operations.dto.DashboardSummaryResponse;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.AgentStatus;
import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.model.IncidentPriority;
import com.seguranca.plataforma.operations.model.IncidentStatus;
import com.seguranca.plataforma.operations.model.IncidentType;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.model.ShiftStatus;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleStatus;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OperationsService {

    private final Map<Long, Agent> agents = new LinkedHashMap<>();
    private final Map<Long, Vehicle> vehicles = new LinkedHashMap<>();
    private final Map<Long, Shift> shifts = new LinkedHashMap<>();
    private final Map<Long, Incident> incidents = new LinkedHashMap<>();

    private final AtomicLong agentSequence = new AtomicLong(0);
    private final AtomicLong vehicleSequence = new AtomicLong(0);
    private final AtomicLong shiftSequence = new AtomicLong(0);
    private final AtomicLong incidentSequence = new AtomicLong(0);

    @PostConstruct
    void seed() {
        Agent carlos = createAgent(new CreateAgentRequest("Carlos Nunes", "ALPHA-01", "AB", LocalDate.now().plusYears(2), null), AgentStatus.ON_DUTY);
        Agent marina = createAgent(new CreateAgentRequest("Marina Luz", "BETA-02", "AB", LocalDate.now().plusYears(3), null), AgentStatus.ACTIVE);
        Agent joao = createAgent(new CreateAgentRequest("Joao Prado", "SUP-01", "B", LocalDate.now().plusYears(1), null), AgentStatus.OFF_DUTY);

        Vehicle alpha = createVehicle(new CreateVehicleRequest("ABC1D23", "Renault Duster", 48241, 49000), VehicleStatus.IN_OPERATION);
        Vehicle beta = createVehicle(new CreateVehicleRequest("FGH4J56", "Chevrolet Spin", 61120, 62000), VehicleStatus.AVAILABLE);

        Shift activeShift = new Shift(
                shiftSequence.incrementAndGet(),
                carlos.id(),
                carlos.fullName(),
                alpha.id(),
                alpha.plate(),
                ShiftStatus.ACTIVE,
                OffsetDateTime.now().minusHours(3),
                OffsetDateTime.now().plusHours(5)
        );
        Shift plannedShift = new Shift(
                shiftSequence.incrementAndGet(),
                marina.id(),
                marina.fullName(),
                beta.id(),
                beta.plate(),
                ShiftStatus.PLANNED,
                OffsetDateTime.now().plusHours(5),
                OffsetDateTime.now().plusHours(13)
        );
        shifts.put(activeShift.id(), activeShift);
        shifts.put(plannedShift.id(), plannedShift);

        Incident panic = new Incident(
                incidentSequence.incrementAndGet(),
                IncidentType.PANIC,
                IncidentPriority.HIGH,
                IncidentStatus.DISPATCHED,
                "Ana Souza",
                "Rua das Acacias, 85",
                OffsetDateTime.now().minusMinutes(9),
                carlos.fullName(),
                alpha.plate()
        );
        Incident escort = new Incident(
                incidentSequence.incrementAndGet(),
                IncidentType.ESCORT,
                IncidentPriority.MEDIUM,
                IncidentStatus.OPEN,
                "Bruno Lima",
                "Alameda Ipe, 210",
                OffsetDateTime.now().minusMinutes(3),
                marina.fullName(),
                beta.plate()
        );
        incidents.put(panic.id(), panic);
        incidents.put(escort.id(), escort);
    }

    public List<Agent> listAgents() {
        return agents.values().stream()
                .sorted(Comparator.comparing(Agent::id))
                .toList();
    }

    public Agent addAgent(CreateAgentRequest request) {
        return createAgent(request, AgentStatus.ACTIVE);
    }

    public List<Vehicle> listVehicles() {
        return vehicles.values().stream()
                .sorted(Comparator.comparing(Vehicle::id))
                .toList();
    }

    public Vehicle addVehicle(CreateVehicleRequest request) {
        return createVehicle(request, VehicleStatus.AVAILABLE);
    }

    public List<Shift> listShifts() {
        return shifts.values().stream()
                .sorted(Comparator.comparing(Shift::startedAt))
                .toList();
    }

    public Shift addShift(CreateShiftRequest request) {
        Agent agent = getAgent(request.agentId());
        Vehicle vehicle = getVehicle(request.vehicleId());

        Shift shift = new Shift(
                shiftSequence.incrementAndGet(),
                agent.id(),
                agent.fullName(),
                vehicle.id(),
                vehicle.plate(),
                ShiftStatus.PLANNED,
                OffsetDateTime.now(),
                request.scheduledEndAt()
        );
        shifts.put(shift.id(), shift);
        return shift;
    }

    public List<Incident> listIncidents() {
        return incidents.values().stream()
                .sorted(Comparator.comparing(Incident::openedAt).reversed())
                .toList();
    }

    public Incident addIncident(CreateIncidentRequest request) {
        String assignedAgentName = request.assignedAgentId() == null ? null : getAgent(request.assignedAgentId()).fullName();
        String vehiclePlate = request.vehicleId() == null ? null : getVehicle(request.vehicleId()).plate();

        Incident incident = new Incident(
                incidentSequence.incrementAndGet(),
                request.type(),
                request.priority(),
                IncidentStatus.OPEN,
                request.residentName(),
                request.address(),
                OffsetDateTime.now(),
                assignedAgentName,
                vehiclePlate
        );
        incidents.put(incident.id(), incident);
        return incident;
    }

    public DashboardSummaryResponse summary() {
        List<Agent> agentList = listAgents();
        List<Vehicle> vehicleList = listVehicles();
        List<Shift> shiftList = listShifts();
        List<Incident> incidentList = listIncidents();

        long activeAgents = agentList.stream()
                .filter(agent -> agent.status() == AgentStatus.ACTIVE || agent.status() == AgentStatus.ON_DUTY)
                .count();
        long availableVehicles = vehicleList.stream()
                .filter(vehicle -> vehicle.status() == VehicleStatus.AVAILABLE || vehicle.status() == VehicleStatus.IN_OPERATION)
                .count();
        long activeShifts = shiftList.stream()
                .filter(shift -> shift.status() == ShiftStatus.ACTIVE || shift.status() == ShiftStatus.HANDOFF)
                .count();
        long openIncidents = incidentList.stream()
                .filter(incident -> incident.status() != IncidentStatus.CLOSED)
                .count();
        long maintenanceAlerts = vehicleList.stream()
                .filter(vehicle -> vehicle.nextMaintenanceKm() - vehicle.currentKm() <= 1000)
                .count();

        return new DashboardSummaryResponse(
                agentList.size(),
                activeAgents,
                availableVehicles,
                activeShifts,
                openIncidents,
                maintenanceAlerts,
                new ArrayList<>(agentList),
                new ArrayList<>(vehicleList),
                new ArrayList<>(shiftList),
                new ArrayList<>(incidentList)
        );
    }

    private Agent createAgent(CreateAgentRequest request, AgentStatus status) {
        Agent agent = new Agent(
                agentSequence.incrementAndGet(),
                request.fullName(),
                request.badgeCode(),
                request.cnhCategory(),
                request.cnhExpiry(),
                status,
                request.photoUrl()
        );
        agents.put(agent.id(), agent);
        return agent;
    }

    private Vehicle createVehicle(CreateVehicleRequest request, VehicleStatus status) {
        Vehicle vehicle = new Vehicle(
                vehicleSequence.incrementAndGet(),
                request.plate().toUpperCase(),
                request.model(),
                request.currentKm(),
                request.nextMaintenanceKm(),
                status
        );
        vehicles.put(vehicle.id(), vehicle);
        return vehicle;
    }

    private Agent getAgent(Long id) {
        Agent agent = agents.get(id);
        if (agent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente nao encontrado");
        }
        return agent;
    }

    private Vehicle getVehicle(Long id) {
        Vehicle vehicle = vehicles.get(id);
        if (vehicle == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Viatura nao encontrada");
        }
        return vehicle;
    }
}
