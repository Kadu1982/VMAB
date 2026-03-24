package com.seguranca.plataforma.operations.service;

import com.seguranca.plataforma.operations.dto.CreateAgentRequest;
import com.seguranca.plataforma.operations.dto.ActivePatrolResponse;
import com.seguranca.plataforma.operations.dto.CreateIncidentRequest;
import com.seguranca.plataforma.operations.dto.CreateResidentRequest;
import com.seguranca.plataforma.operations.dto.CreateShiftRequest;
import com.seguranca.plataforma.operations.dto.CreateVehicleRequest;
import com.seguranca.plataforma.operations.dto.ClientPortalResponse;
import com.seguranca.plataforma.operations.dto.DashboardSummaryResponse;
import com.seguranca.plataforma.operations.dto.HandoffShiftRequest;
import com.seguranca.plataforma.operations.dto.PatrolRouteStopResponse;
import com.seguranca.plataforma.operations.dto.TelemetryTrailPointResponse;
import com.seguranca.plataforma.operations.dto.UpdateAgentRequest;
import com.seguranca.plataforma.operations.dto.UpdateIncidentRequest;
import com.seguranca.plataforma.operations.dto.UpdateResidentRequest;
import com.seguranca.plataforma.operations.dto.UpdateShiftRequest;
import com.seguranca.plataforma.operations.dto.UpdateVehicleRequest;
import com.seguranca.plataforma.operations.dto.UpsertShiftTelemetryRequest;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.AgentStatus;
import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.model.IncidentStatus;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.model.ResidentStatus;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.model.ShiftAttendanceStatus;
import com.seguranca.plataforma.operations.model.ShiftStatus;
import com.seguranca.plataforma.operations.model.ShiftTelemetry;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleStatus;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.IncidentRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.operations.repository.ShiftRepository;
import com.seguranca.plataforma.operations.repository.ShiftTelemetryRepository;
import com.seguranca.plataforma.operations.repository.VehicleRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OperationsService {
    // Orquestra o dominio operacional: cadastros, turnos, ocorrencias, dashboard e telemetria.

    private final AgentRepository agentRepository;
    private final VehicleRepository vehicleRepository;
    private final ResidentRepository residentRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftTelemetryRepository shiftTelemetryRepository;
    private final IncidentRepository incidentRepository;

    public OperationsService(
            AgentRepository agentRepository,
            VehicleRepository vehicleRepository,
            ResidentRepository residentRepository,
            ShiftRepository shiftRepository,
            ShiftTelemetryRepository shiftTelemetryRepository,
            IncidentRepository incidentRepository
    ) {
        this.agentRepository = agentRepository;
        this.vehicleRepository = vehicleRepository;
        this.residentRepository = residentRepository;
        this.shiftRepository = shiftRepository;
        this.shiftTelemetryRepository = shiftTelemetryRepository;
        this.incidentRepository = incidentRepository;
    }

    @PostConstruct
    @Transactional
    void seed() {
        // Semeia um ambiente minimo para demonstracao e testes locais.
        if (agentRepository.count() > 0 || vehicleRepository.count() > 0 || residentRepository.count() > 0 || shiftRepository.count() > 0 || incidentRepository.count() > 0) {
            return;
        }

        Agent carlos = agentRepository.save(new Agent("Carlos Nunes", "ALPHA-01", "AB", LocalDate.now().plusYears(2), AgentStatus.ON_DUTY, "https://i.pravatar.cc/160?img=12", LocalDate.now().plusMonths(8), LocalDate.now().plusMonths(6), "Exames ocupacionais em dia"));
        Agent marina = agentRepository.save(new Agent("Marina Luz", "BETA-02", "AB", LocalDate.now().plusYears(3), AgentStatus.ACTIVE, "https://i.pravatar.cc/160?img=32", LocalDate.now().plusMonths(10), LocalDate.now().plusMonths(7), "Apta para cobertura noturna"));
        agentRepository.save(new Agent("Joao Prado", "SUP-01", "B", LocalDate.now().plusYears(1), AgentStatus.OFF_DUTY, null, LocalDate.now().plusMonths(4), LocalDate.now().plusMonths(5), "Necessita reciclagem semestral"));
        Resident ana = residentRepository.save(new Resident("Ana Souza", "(11) 99888-1122", "Rua das Acacias, 85", "Casa azul com portao branco", ResidentStatus.ACTIVE));
        Resident bruno = residentRepository.save(new Resident("Bruno Lima", "(11) 99777-6655", "Alameda Ipe, 210", "Acesso lateral pela guarita 2", ResidentStatus.ACTIVE));

        Vehicle alpha = vehicleRepository.save(new Vehicle("ABC1D23", "Renault Duster", 48241, 49000, VehicleStatus.IN_OPERATION, LocalDate.now().plusMonths(7), LocalDate.now().plusMonths(7), LocalDate.now().plusMonths(10), LocalDate.now().minusMonths(2), "Manutencao preventiva realizada na ultima troca de oleo"));
        Vehicle beta = vehicleRepository.save(new Vehicle("FGH4J56", "Chevrolet Spin", 61120, 62000, VehicleStatus.AVAILABLE, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(6), LocalDate.now().minusMonths(1), "Verificar desgaste de pneus no proximo ciclo"));

        Shift activeSeedShift = new Shift(
                carlos.getId(),
                carlos.getFullName(),
                alpha.getId(),
                alpha.getPlate(),
                ShiftStatus.ACTIVE,
                OffsetDateTime.now().minusHours(3),
                OffsetDateTime.now().minusHours(3),
                OffsetDateTime.now().plusHours(5),
                OffsetDateTime.now().minusHours(3),
                alpha.getCurrentKm(),
                76,
                true,
                true,
                true,
                "Checklist inicial validado pela base"
        );
        applyAttendanceState(activeSeedShift, null, null, "Turno iniciado dentro do horario previsto");
        shiftRepository.save(activeSeedShift);

        Shift plannedSeedShift = new Shift(
                marina.getId(),
                marina.getFullName(),
                beta.getId(),
                beta.getPlate(),
                ShiftStatus.PLANNED,
                null,
                OffsetDateTime.now().plusHours(5),
                OffsetDateTime.now().plusHours(13),
                null,
                null,
                54,
                true,
                true,
                false,
                "Documentacao da viatura precisa ser revisada antes do proximo turno"
        );
        applyAttendanceState(plannedSeedShift, ShiftAttendanceStatus.PENDING, null, "Aguardando apresentacao da equipe de cobertura");
        shiftRepository.save(plannedSeedShift);

        incidentRepository.save(new Incident(
                com.seguranca.plataforma.operations.model.IncidentType.PANIC,
                com.seguranca.plataforma.operations.model.IncidentPriority.HIGH,
                IncidentStatus.DISPATCHED,
                ana.getFullName(),
                ana.getAddress(),
                OffsetDateTime.now().minusMinutes(9),
                carlos.getFullName(),
                alpha.getPlate()
        ));
        incidentRepository.save(new Incident(
                com.seguranca.plataforma.operations.model.IncidentType.ESCORT,
                com.seguranca.plataforma.operations.model.IncidentPriority.MEDIUM,
                IncidentStatus.OPEN,
                bruno.getFullName(),
                bruno.getAddress(),
                OffsetDateTime.now().minusMinutes(3),
                marina.getFullName(),
                beta.getPlate()
        ));
    }

    @Transactional(readOnly = true)
    public List<Agent> listAgents() {
        return agentRepository.findAll().stream()
                .sorted(Comparator.comparing(Agent::getId))
                .toList();
    }

    @Transactional
    public Agent addAgent(CreateAgentRequest request) {
        Agent agent = new Agent(
                request.fullName(),
                request.badgeCode(),
                request.cnhCategory(),
                request.cnhExpiry(),
                AgentStatus.ACTIVE,
                request.photoUrl(),
                request.medicalExamExpiry(),
                request.workExamsExpiry(),
                request.documentNotes()
        );
        return agentRepository.save(agent);
    }

    @Transactional
    public Agent updateAgent(Long id, UpdateAgentRequest request) {
        Agent agent = getAgent(id);
        agent.update(
                request.fullName(),
                request.badgeCode(),
                request.cnhCategory(),
                request.cnhExpiry(),
                request.status(),
                request.photoUrl(),
                request.medicalExamExpiry(),
                request.workExamsExpiry(),
                request.documentNotes()
        );
        return agentRepository.save(agent);
    }

    @Transactional
    public void deleteAgent(Long id) {
        Agent agent = getAgent(id);
        agentRepository.delete(agent);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listVehicles() {
        return vehicleRepository.findAll().stream()
                .sorted(Comparator.comparing(Vehicle::getId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Resident> listResidents() {
        return residentRepository.findAll().stream()
                .sorted(Comparator.comparing(Resident::getId))
                .toList();
    }

    @Transactional
    public Resident addResident(CreateResidentRequest request) {
        Resident resident = new Resident(
                request.fullName(),
                request.phoneNumber(),
                request.address(),
                request.referenceNote(),
                ResidentStatus.ACTIVE
        );
        return residentRepository.save(resident);
    }

    @Transactional
    public Resident updateResident(Long id, UpdateResidentRequest request) {
        Resident resident = getResident(id);
        resident.update(
                request.fullName(),
                request.phoneNumber(),
                request.address(),
                request.referenceNote(),
                request.status()
        );
        return residentRepository.save(resident);
    }

    @Transactional
    public void deleteResident(Long id) {
        Resident resident = getResident(id);
        residentRepository.delete(resident);
    }

    @Transactional
    public Vehicle addVehicle(CreateVehicleRequest request) {
        Vehicle vehicle = new Vehicle(
                request.plate().toUpperCase(),
                request.model(),
                request.currentKm(),
                request.nextMaintenanceKm(),
                VehicleStatus.AVAILABLE,
                request.ipvaExpiry(),
                request.licensingExpiry(),
                request.insuranceExpiry(),
                request.lastMaintenanceAt(),
                request.maintenanceNotes()
        );
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle updateVehicle(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = getVehicle(id);
        vehicle.update(
                request.plate().toUpperCase(),
                request.model(),
                request.currentKm(),
                request.nextMaintenanceKm(),
                request.status(),
                request.ipvaExpiry(),
                request.licensingExpiry(),
                request.insuranceExpiry(),
                request.lastMaintenanceAt(),
                request.maintenanceNotes()
        );
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicle(id);
        vehicleRepository.delete(vehicle);
    }

    @Transactional(readOnly = true)
    public List<Shift> listShifts() {
        return shiftRepository.findAll().stream()
                .sorted(Comparator.comparing(Shift::getScheduledStartAt))
                .toList();
    }

    @Transactional
    public Shift addShift(CreateShiftRequest request) {
        // Um turno planejado nao pode nascer com ponto iniciado nem KM de saida preenchido.
        Agent agent = getAgent(request.agentId());
        Vehicle vehicle = getVehicle(request.vehicleId());
        validateFuelLevelPercent(request.fuelLevelPercent());
        validateShiftSchedule(request.scheduledStartAt(), request.scheduledEndAt());
        validateAttendanceConsistency(request.attendanceStatus(), request.coverageForAgentId());
        Agent coveredAgent = resolveCoveredAgent(request.attendanceStatus(), request.coverageForAgentId());

        Shift shift = new Shift(
                agent.getId(),
                agent.getFullName(),
                vehicle.getId(),
                vehicle.getPlate(),
                ShiftStatus.PLANNED,
                null,
                request.scheduledStartAt(),
                request.scheduledEndAt(),
                null,
                null,
                request.fuelLevelPercent(),
                request.tiresChecked(),
                request.lightsChecked(),
                request.documentsChecked(),
                request.checklistNotes()
        );
        applyAttendanceState(shift, request.attendanceStatus(), coveredAgent, request.attendanceNotes());
        return shiftRepository.save(shift);
    }

    @Transactional
    public Shift updateShift(Long id, UpdateShiftRequest request) {
        // Atualiza o turno e exige KM final quando o fluxo estiver sendo encerrado.
        Shift shift = getShift(id);
        Agent agent = getAgent(request.agentId());
        Vehicle vehicle = getVehicle(request.vehicleId());
        validateFuelLevelPercent(request.fuelLevelPercent());
        validateShiftSchedule(request.scheduledStartAt(), request.scheduledEndAt());
        validateAttendanceConsistency(request.attendanceStatus(), request.coverageForAgentId());
        Agent coveredAgent = resolveCoveredAgent(request.attendanceStatus(), request.coverageForAgentId());

        if (request.attendanceStatus() == ShiftAttendanceStatus.ABSENT
                && (request.status() == ShiftStatus.ACTIVE || request.status() == ShiftStatus.HANDOFF || request.status() == ShiftStatus.CLOSED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um turno marcado como falta nao pode entrar em operacao sem cobertura.");
        }

        if (request.status() == ShiftStatus.ACTIVE || request.status() == ShiftStatus.HANDOFF || request.status() == ShiftStatus.CLOSED) {
            activateShiftIfNeeded(shift, vehicle);
        }

        if (request.status() == ShiftStatus.CLOSED && request.endKm() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a quilometragem final para encerrar o turno.");
        }

        if (request.status() == ShiftStatus.CLOSED) {
            validateClosingKm(shift, vehicle, request.endKm());
        }

        if (request.status() == ShiftStatus.CLOSED && (!request.tiresChecked() || !request.lightsChecked() || !request.documentsChecked())) {
            // Impede encerramento superficial do turno sem conferencia minima da viatura.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conclua o checklist minimo da viatura antes de encerrar o turno.");
        }

        shift.update(
                agent.getId(),
                agent.getFullName(),
                vehicle.getId(),
                vehicle.getPlate(),
                request.status(),
                request.scheduledStartAt(),
                request.scheduledEndAt(),
                request.endKm(),
                request.fuelLevelPercent(),
                request.tiresChecked(),
                request.lightsChecked(),
                request.documentsChecked(),
                request.checklistNotes()
        );
        applyAttendanceState(shift, request.attendanceStatus(), coveredAgent, request.attendanceNotes());

        if (request.status() == ShiftStatus.CLOSED) {
            shift.close(OffsetDateTime.now(ZoneOffset.UTC), request.endKm());
            vehicle.update(
                    vehicle.getPlate(),
                    vehicle.getModel(),
                    request.endKm(),
                    vehicle.getNextMaintenanceKm(),
                    vehicle.getStatus(),
                    vehicle.getIpvaExpiry(),
                    vehicle.getLicensingExpiry(),
                    vehicle.getInsuranceExpiry(),
                    vehicle.getLastMaintenanceAt(),
                    vehicle.getMaintenanceNotes()
            );
            vehicleRepository.save(vehicle);
        }

        return shiftRepository.save(shift);
    }

    @Transactional
    public Shift handoffShift(Long id, HandoffShiftRequest request) {
        // Formaliza a troca de vigilante em um turno ja existente sem abrir outro registro paralelo.
        Shift shift = getShift(id);
        if (!shift.getAgentId().equals(request.fromAgentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O agente de origem nao corresponde ao turno atual.");
        }

        Agent newAgent = getAgent(request.toAgentId());
        Vehicle vehicle = getVehicle(shift.getVehicleId());
        activateShiftIfNeeded(shift, vehicle);
        shift.registerHandoff(
                shift.getAgentId(),
                shift.getAgentName(),
                newAgent.getId(),
                newAgent.getFullName(),
                OffsetDateTime.now(ZoneOffset.UTC),
                request.notes()
        );

        return shiftRepository.save(shift);
    }

    @Transactional
    public void deleteShift(Long id) {
        Shift shift = getShift(id);
        shiftRepository.delete(shift);
    }

    @Transactional
    public ShiftTelemetry upsertShiftTelemetry(Long shiftId, UpsertShiftTelemetryRequest request) {
        // Cada sincronizacao do mobile vira um ponto historico novo para desenhar a trilha real.
        Shift shift = getShift(shiftId);
        OffsetDateTime recordedAt = request.recordedAt() != null ? request.recordedAt() : OffsetDateTime.now();

        ShiftTelemetry telemetry = new ShiftTelemetry(
                shift.getId(),
                shift.getAgentId(),
                shift.getVehicleId(),
                request.latitude(),
                request.longitude(),
                request.speedKmh(),
                request.accuracyMeters(),
                request.headingDegrees(),
                request.batteryLevel(),
                recordedAt
        );
        return shiftTelemetryRepository.save(telemetry);
    }

    @Transactional(readOnly = true)
    public ShiftTelemetry getShiftTelemetry(Long shiftId) {
        getShift(shiftId);
        return shiftTelemetryRepository.findTopByShiftIdOrderByRecordedAtDesc(shiftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telemetria nao encontrada para o turno"));
    }

    @Transactional(readOnly = true)
    public List<Incident> listIncidents() {
        return incidentRepository.findAll().stream()
                .sorted(Comparator.comparing(Incident::getOpenedAt).reversed())
                .toList();
    }

    @Transactional
    public Incident addIncident(CreateIncidentRequest request) {
        Resident resident = resolveResident(request.residentId(), request.residentName(), request.address());
        String assignedAgentName = request.assignedAgentId() == null ? null : getAgent(request.assignedAgentId()).getFullName();
        String vehiclePlate = request.vehicleId() == null ? null : getVehicle(request.vehicleId()).getPlate();

        Incident incident = new Incident(
                request.type(),
                request.priority(),
                IncidentStatus.OPEN,
                resident != null ? resident.getFullName() : request.residentName().trim(),
                resident != null ? resident.getAddress() : request.address().trim(),
                OffsetDateTime.now(),
                assignedAgentName,
                vehiclePlate
        );
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident updateIncident(Long id, UpdateIncidentRequest request) {
        Incident incident = getIncident(id);
        Resident resident = resolveResident(request.residentId(), request.residentName(), request.address());
        String assignedAgentName = request.assignedAgentId() == null ? null : getAgent(request.assignedAgentId()).getFullName();
        String vehiclePlate = request.vehicleId() == null ? null : getVehicle(request.vehicleId()).getPlate();

        incident.update(
                request.type(),
                request.priority(),
                request.status(),
                resident != null ? resident.getFullName() : request.residentName().trim(),
                resident != null ? resident.getAddress() : request.address().trim(),
                assignedAgentName,
                vehiclePlate
        );
        return incidentRepository.save(incident);
    }

    @Transactional
    public void deleteIncident(Long id) {
        Incident incident = getIncident(id);
        incidentRepository.delete(incident);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        // Consolida o estado operacional em uma unica resposta para reduzir chamadas do dashboard.
        List<Resident> residents = listResidents();
        List<Agent> agents = listAgents();
        List<Vehicle> vehicles = listVehicles();
        List<Shift> shifts = listShifts();
        List<Incident> incidents = listIncidents();

        long activeAgents = agents.stream()
                .filter(agent -> agent.getStatus() == AgentStatus.ACTIVE || agent.getStatus() == AgentStatus.ON_DUTY)
                .count();
        long availableVehicles = vehicles.stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE || vehicle.getStatus() == VehicleStatus.IN_OPERATION)
                .count();
        long activeShifts = shifts.stream()
                .filter(shift -> shift.getStatus() == ShiftStatus.ACTIVE || shift.getStatus() == ShiftStatus.HANDOFF)
                .count();
        long openIncidents = incidents.stream()
                .filter(incident -> incident.getStatus() != IncidentStatus.CLOSED)
                .count();
        long maintenanceAlerts = vehicles.stream()
                .filter(this::hasVehicleAlert)
                .count();
        ActivePatrolResponse activePatrol = buildActivePatrol(agents, vehicles, shifts, incidents);

        return new DashboardSummaryResponse(
                residents.size(),
                agents.size(),
                activeAgents,
                availableVehicles,
                activeShifts,
                openIncidents,
                maintenanceAlerts,
                activePatrol,
                residents,
                agents,
                vehicles,
                shifts,
                incidents
        );
    }

    @Transactional(readOnly = true)
    public ClientPortalResponse clientPortal() {
        // Entrega uma visao simplificada para o cliente sem expor cadastros administrativos.
        List<Vehicle> vehicles = listVehicles();
        List<Shift> shifts = listShifts();
        List<Incident> incidents = listIncidents();

        long availableVehicles = vehicles.stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE || vehicle.getStatus() == VehicleStatus.IN_OPERATION)
                .count();
        long activeShifts = shifts.stream()
                .filter(shift -> shift.getStatus() == ShiftStatus.ACTIVE || shift.getStatus() == ShiftStatus.HANDOFF)
                .count();
        long openIncidents = incidents.stream()
                .filter(incident -> incident.getStatus() != IncidentStatus.CLOSED)
                .count();
        long maintenanceAlerts = vehicles.stream()
                .filter(this::hasVehicleAlert)
                .count();

        return new ClientPortalResponse(
                activeShifts,
                openIncidents,
                availableVehicles,
                maintenanceAlerts,
                incidents.stream().limit(5).toList()
        );
    }

    private Agent getAgent(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente nao encontrado"));
    }

    private Resident getResident(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador nao encontrado"));
    }

    private Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viatura nao encontrada"));
    }

    private boolean hasVehicleAlert(Vehicle vehicle) {
        // Concentra os alertas de manutencao e vencimento documental da viatura no dashboard.
        LocalDate threshold = LocalDate.now().plusDays(30);
        return vehicle.getNextMaintenanceKm() - vehicle.getCurrentKm() <= 1000
                || (vehicle.getIpvaExpiry() != null && !vehicle.getIpvaExpiry().isAfter(threshold))
                || (vehicle.getLicensingExpiry() != null && !vehicle.getLicensingExpiry().isAfter(threshold))
                || (vehicle.getInsuranceExpiry() != null && !vehicle.getInsuranceExpiry().isAfter(threshold));
    }

    private Shift getShift(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turno nao encontrado"));
    }

    private void validateFuelLevelPercent(Integer fuelLevelPercent) {
        // O backend precisa validar a faixa de combustivel mesmo quando a API for chamada fora do formulario web.
        if (fuelLevelPercent != null && (fuelLevelPercent < 0 || fuelLevelPercent > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O combustivel deve estar entre 0 e 100 por cento.");
        }
    }

    private void validateShiftSchedule(OffsetDateTime scheduledStartAt, OffsetDateTime scheduledEndAt) {
        // Garante uma janela de escala coerente antes de persistir o turno.
        if (!scheduledEndAt.isAfter(scheduledStartAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O fim previsto do turno deve ser posterior ao inicio previsto.");
        }
    }

    private void validateAttendanceConsistency(ShiftAttendanceStatus attendanceStatus, Long coverageForAgentId) {
        // Impede estados de escala contraditorios, como cobertura sem vigilante ausente.
        if (attendanceStatus == ShiftAttendanceStatus.COVERED && coverageForAgentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o vigilante coberto quando o turno estiver marcado como cobertura.");
        }

        if (attendanceStatus != ShiftAttendanceStatus.COVERED && coverageForAgentId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O vigilante coberto so pode ser informado quando o status de presenca for cobertura.");
        }
    }

    private void activateShiftIfNeeded(Shift shift, Vehicle vehicle) {
        // Marca check-in e KM inicial apenas quando o turno efetivamente entra em operacao.
        if (shift.getCheckInAt() == null) {
            shift.beginOperationalTracking(OffsetDateTime.now(ZoneOffset.UTC), vehicle.getCurrentKm());
        }
    }

    private void validateClosingKm(Shift shift, Vehicle vehicle, Long endKm) {
        // Impede retrocesso de odometro e fechamento incoerente do turno.
        Long startKm = shift.getStartKm();
        if (startKm != null && endKm < startKm) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quilometragem final nao pode ser menor que a quilometragem inicial do turno.");
        }

        if (endKm < vehicle.getCurrentKm()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quilometragem final nao pode ser menor que a quilometragem atual da viatura.");
        }
    }

    private Agent resolveCoveredAgent(ShiftAttendanceStatus attendanceStatus, Long coverageForAgentId) {
        if (attendanceStatus != ShiftAttendanceStatus.COVERED || coverageForAgentId == null) {
            return null;
        }

        return getAgent(coverageForAgentId);
    }

    private void applyAttendanceState(Shift shift, ShiftAttendanceStatus requestedAttendanceStatus, Agent coveredAgent, String attendanceNotes) {
        // Consolida leitura de escala, atraso e cobertura no proprio turno para evitar estado espalhado.
        ShiftAttendanceStatus effectiveStatus = requestedAttendanceStatus == null ? ShiftAttendanceStatus.PENDING : requestedAttendanceStatus;
        Integer lateMinutes = calculateLateMinutes(shift);

        if ((effectiveStatus == ShiftAttendanceStatus.PENDING || effectiveStatus == ShiftAttendanceStatus.ON_TIME || effectiveStatus == ShiftAttendanceStatus.LATE)
                && shift.getCheckInAt() != null) {
            effectiveStatus = lateMinutes != null && lateMinutes > 0 ? ShiftAttendanceStatus.LATE : ShiftAttendanceStatus.ON_TIME;
        }

        if (effectiveStatus == ShiftAttendanceStatus.PENDING && shift.getCheckInAt() == null) {
            lateMinutes = null;
        }

        shift.updateAttendance(
                effectiveStatus,
                lateMinutes,
                coveredAgent != null ? coveredAgent.getId() : null,
                coveredAgent != null ? coveredAgent.getFullName() : null,
                attendanceNotes
        );
    }

    private Integer calculateLateMinutes(Shift shift) {
        if (shift.getCheckInAt() == null || shift.getScheduledStartAt() == null) {
            return null;
        }

        long minutes = java.time.Duration.between(shift.getScheduledStartAt(), shift.getCheckInAt()).toMinutes();
        return (int) Math.max(minutes, 0);
    }

    private Incident getIncident(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ocorrencia nao encontrada"));
    }

    private ActivePatrolResponse buildActivePatrol(List<Agent> agents, List<Vehicle> vehicles, List<Shift> shifts, List<Incident> incidents) {
        // Monta o cartao e o mapa da patrulha ativa com ultimo ponto e historico de telemetria.
        Shift activeShift = shifts.stream()
                .filter(shift -> shift.getStatus() == ShiftStatus.ACTIVE || shift.getStatus() == ShiftStatus.HANDOFF)
                .findFirst()
                .orElse(null);

        if (activeShift == null) {
            return null;
        }

        Map<Long, Agent> agentsById = agents.stream().collect(java.util.stream.Collectors.toMap(Agent::getId, agent -> agent));
        Map<Long, Vehicle> vehiclesById = vehicles.stream().collect(java.util.stream.Collectors.toMap(Vehicle::getId, vehicle -> vehicle));

        Agent agent = agentsById.get(activeShift.getAgentId());
        Vehicle vehicle = vehiclesById.get(activeShift.getVehicleId());
        List<ShiftTelemetry> telemetryHistory = shiftTelemetryRepository.findByShiftIdOrderByRecordedAtAsc(activeShift.getId());
        ShiftTelemetry telemetry = telemetryHistory.isEmpty() ? null : telemetryHistory.get(telemetryHistory.size() - 1);
        Incident targetIncident = incidents.stream()
                .filter(incident -> incident.getStatus() != IncidentStatus.CLOSED)
                .findFirst()
                .orElse(null);
        double traveledKmInShift = calculateTraveledKmInShift(telemetryHistory);
        int progressPercent = telemetry == null ? 0 : 100;
        List<TelemetryTrailPointResponse> telemetryTrail = telemetryHistory.stream()
                .map(point -> new TelemetryTrailPointResponse(
                        point.getLatitude(),
                        point.getLongitude(),
                        point.getSpeedKmh(),
                        point.getAccuracyMeters(),
                        point.getRecordedAt()
                ))
                .toList();

        List<PatrolRouteStopResponse> routeStops = List.of(
                new PatrolRouteStopResponse("Base Alpha", "Saida da base operacional", telemetry == null ? "Aguardando GPS" : "Inicio confirmado"),
                new PatrolRouteStopResponse("Corredor principal", "Rastreamento ativo via GPS do celular da viatura", telemetry == null ? "Sem sinal recente" : "Posicao recebida"),
                new PatrolRouteStopResponse(
                        targetIncident != null ? targetIncident.getAddress() : "Destino de ronda",
                        targetIncident != null ? targetIncident.getResidentName() : activeShift.getAgentName(),
                        targetIncident != null ? targetIncident.getStatus().name() : activeShift.getStatus().name()
                )
        );

        return new ActivePatrolResponse(
                activeShift.getId(),
                agent != null ? agent.getId() : activeShift.getAgentId(),
                agent != null ? agent.getFullName() : activeShift.getAgentName(),
                agent != null ? agent.getBadgeCode() : "SEM-CRACHA",
                agent != null ? agent.getPhotoUrl() : null,
                vehicle != null ? vehicle.getPlate() : activeShift.getVehiclePlate(),
                vehicle != null ? vehicle.getModel() : "Viatura em operacao",
                vehicle != null ? vehicle.getCurrentKm() : 0,
                vehicle != null ? vehicle.getStatus().name() : VehicleStatus.IN_OPERATION.name(),
                telemetry != null ? telemetry.getLatitude() : -23.56390,
                telemetry != null ? telemetry.getLongitude() : -46.65440,
                telemetry != null ? telemetry.getSpeedKmh() : 0.0,
                telemetry != null ? telemetry.getAccuracyMeters() : 0.0,
                traveledKmInShift,
                progressPercent,
                telemetry != null ? telemetry.getRecordedAt() : OffsetDateTime.now(),
                routeStops,
                telemetryTrail
        );
    }

    private double calculateTraveledKmInShift(List<ShiftTelemetry> telemetryHistory) {
        if (telemetryHistory.size() < 2) {
            return 0.0;
        }

        double totalMeters = 0.0;
        for (int index = 1; index < telemetryHistory.size(); index++) {
            ShiftTelemetry previous = telemetryHistory.get(index - 1);
            ShiftTelemetry current = telemetryHistory.get(index);
            totalMeters += haversineMeters(
                    previous.getLatitude(),
                    previous.getLongitude(),
                    current.getLatitude(),
                    current.getLongitude()
            );
        }

        return totalMeters / 1000.0;
    }

    private double haversineMeters(double startLat, double startLon, double endLat, double endLon) {
        double earthRadiusMeters = 6_371_000.0;
        double latDelta = Math.toRadians(endLat - startLat);
        double lonDelta = Math.toRadians(endLon - startLon);
        double originLat = Math.toRadians(startLat);
        double destinationLat = Math.toRadians(endLat);

        double a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2)
                + Math.cos(originLat) * Math.cos(destinationLat)
                * Math.sin(lonDelta / 2) * Math.sin(lonDelta / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }

    private Resident resolveResident(Long residentId, String residentName, String address) {
        if (residentId != null) {
            return getResident(residentId);
        }

        if (!StringUtils.hasText(residentName) || !StringUtils.hasText(address)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Morador e endereco sao obrigatorios quando nao houver cadastro selecionado");
        }

        return null;
    }
}
