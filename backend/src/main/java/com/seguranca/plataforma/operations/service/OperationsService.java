package com.seguranca.plataforma.operations.service;

import com.seguranca.plataforma.auth.AppUser;
import com.seguranca.plataforma.auth.AppUserPushNotificationService;
import com.seguranca.plataforma.auth.AppUserRepository;
import com.seguranca.plataforma.auth.AppUserRole;
import com.seguranca.plataforma.config.VmabRetentionProperties;
import com.seguranca.plataforma.operations.dto.CreateAgentRequest;
import com.seguranca.plataforma.operations.dto.ActivePatrolResponse;
import com.seguranca.plataforma.operations.dto.CreateIncidentRequest;
import com.seguranca.plataforma.operations.dto.CloseIncidentRequest;
import com.seguranca.plataforma.operations.dto.CreateResidentRequest;
import com.seguranca.plataforma.operations.dto.CreateShiftRequest;
import com.seguranca.plataforma.operations.dto.CreateVehicleMaintenanceRequest;
import com.seguranca.plataforma.operations.dto.CreateVehicleRequest;
import com.seguranca.plataforma.operations.dto.DispatchIncidentRequest;
import com.seguranca.plataforma.operations.dto.FleetOperationalReportResponse;
import com.seguranca.plataforma.operations.dto.IncidentEvidenceResponse;
import com.seguranca.plataforma.operations.dto.ClientPortalResponse;
import com.seguranca.plataforma.operations.dto.DashboardSummaryResponse;
import com.seguranca.plataforma.operations.dto.PatrolRouteStopResponse;
import com.seguranca.plataforma.operations.dto.OnSiteIncidentRequest;
import com.seguranca.plataforma.operations.dto.RondaCloseIncidentRequest;
import com.seguranca.plataforma.operations.dto.RondaDispatchIncidentRequest;
import com.seguranca.plataforma.operations.dto.RequestShiftHandoffRequest;
import com.seguranca.plataforma.operations.dto.RespondShiftHandoffRequest;
import com.seguranca.plataforma.operations.dto.ShiftSupervisionAction;
import com.seguranca.plataforma.operations.dto.SuperviseShiftRequest;
import com.seguranca.plataforma.operations.dto.TelemetryTrailPointResponse;
import com.seguranca.plataforma.operations.dto.UpdateAgentRequest;
import com.seguranca.plataforma.operations.dto.UpdateIncidentRequest;
import com.seguranca.plataforma.operations.dto.UpdateResidentRequest;
import com.seguranca.plataforma.operations.dto.UpdateShiftRequest;
import com.seguranca.plataforma.operations.dto.UpdateVehicleMaintenanceRequest;
import com.seguranca.plataforma.operations.dto.UpdateVehicleRequest;
import com.seguranca.plataforma.operations.dto.UpsertShiftTelemetryRequest;
import com.seguranca.plataforma.operations.dto.VehicleMaintenanceOrderResponse;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.AgentStatus;
import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.model.Incident;
import com.seguranca.plataforma.operations.model.IncidentEvidence;
import com.seguranca.plataforma.operations.model.IncidentStatus;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.model.ResidentStatus;
import com.seguranca.plataforma.operations.model.Shift;
import com.seguranca.plataforma.operations.model.ShiftAttendanceStatus;
import com.seguranca.plataforma.operations.model.ShiftStatus;
import com.seguranca.plataforma.operations.model.ShiftTelemetry;
import com.seguranca.plataforma.operations.model.Vehicle;
import com.seguranca.plataforma.operations.model.VehicleMaintenancePriority;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceRecord;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceStatus;
import com.seguranca.plataforma.operations.model.VehicleMaintenanceType;
import com.seguranca.plataforma.operations.model.VehicleStatus;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.IncidentRepository;
import com.seguranca.plataforma.operations.repository.IncidentEvidenceRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.operations.repository.ShiftRepository;
import com.seguranca.plataforma.operations.repository.ShiftTelemetryRepository;
import com.seguranca.plataforma.operations.repository.VehicleMaintenanceRecordRepository;
import com.seguranca.plataforma.operations.repository.VehicleRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OperationsService {
    // Orquestra o dominio operacional: cadastros, turnos, ocorrencias, dashboard e telemetria.

    private final AgentRepository agentRepository;
    private final AppUserRepository appUserRepository;
    private final AppUserPushNotificationService appUserPushNotificationService;
    private final AuditRecordRepository auditRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleMaintenanceRecordRepository vehicleMaintenanceRecordRepository;
    private final ResidentRepository residentRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftTelemetryRepository shiftTelemetryRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentEvidenceRepository incidentEvidenceRepository;
    private final OperationsRealtimeService operationsRealtimeService;
    private final VehicleFleetReportCalculator vehicleFleetReportCalculator;
    private final VmabRetentionProperties retentionProperties;
    private final PasswordEncoder passwordEncoder;
    private final Path storageRoot;

    public OperationsService(
            AgentRepository agentRepository,
            AppUserRepository appUserRepository,
            AppUserPushNotificationService appUserPushNotificationService,
            AuditRecordRepository auditRecordRepository,
            VehicleRepository vehicleRepository,
            VehicleMaintenanceRecordRepository vehicleMaintenanceRecordRepository,
            ResidentRepository residentRepository,
            ShiftRepository shiftRepository,
            ShiftTelemetryRepository shiftTelemetryRepository,
            IncidentRepository incidentRepository,
            IncidentEvidenceRepository incidentEvidenceRepository,
            OperationsRealtimeService operationsRealtimeService,
            VehicleFleetReportCalculator vehicleFleetReportCalculator,
            VmabRetentionProperties retentionProperties,
            PasswordEncoder passwordEncoder,
            @Value("${vmab.storage-root}") String storageRoot
    ) {
        this.agentRepository = agentRepository;
        this.appUserRepository = appUserRepository;
        this.appUserPushNotificationService = appUserPushNotificationService;
        this.auditRecordRepository = auditRecordRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleMaintenanceRecordRepository = vehicleMaintenanceRecordRepository;
        this.residentRepository = residentRepository;
        this.shiftRepository = shiftRepository;
        this.shiftTelemetryRepository = shiftTelemetryRepository;
        this.incidentRepository = incidentRepository;
        this.incidentEvidenceRepository = incidentEvidenceRepository;
        this.operationsRealtimeService = operationsRealtimeService;
        this.vehicleFleetReportCalculator = vehicleFleetReportCalculator;
        this.retentionProperties = retentionProperties;
        this.passwordEncoder = passwordEncoder;
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @PostConstruct
    @Transactional
    void seed() {
        // Semeia um ambiente minimo para demonstracao e testes locais.
        initializeStorageDirectories();
        if (agentRepository.count() > 0 || vehicleRepository.count() > 0 || residentRepository.count() > 0 || shiftRepository.count() > 0 || incidentRepository.count() > 0) {
            return;
        }

        Agent carlos = agentRepository.save(new Agent("Carlos Nunes", "ALPHA-01", "AB", LocalDate.now().plusYears(2), AgentStatus.ON_DUTY, "https://i.pravatar.cc/160?img=12", LocalDate.now().plusMonths(8), LocalDate.now().plusMonths(6), "Exames ocupacionais em dia"));
        Agent marina = agentRepository.save(new Agent("Marina Luz", "BETA-02", "AB", LocalDate.now().plusYears(3), AgentStatus.ACTIVE, "https://i.pravatar.cc/160?img=32", LocalDate.now().plusMonths(10), LocalDate.now().plusMonths(7), "Apta para cobertura noturna"));
        agentRepository.save(new Agent("Joao Prado", "SUP-01", "B", LocalDate.now().plusYears(1), AgentStatus.OFF_DUTY, null, LocalDate.now().plusMonths(4), LocalDate.now().plusMonths(5), "Necessita reciclagem semestral"));
        Resident ana = residentRepository.save(new Resident(
                "Ana Souza",
                "(11) 99888-1122",
                "Rua das Acacias, 85",
                "Casa azul com portao branco",
                ResidentStatus.ACTIVE,
                encodeResidentPin(null, "(11) 99888-1122", false),
                encodeResidentPin(null, "(11) 99888-1122", true)
        ));
        Resident bruno = residentRepository.save(new Resident(
                "Bruno Lima",
                "(11) 99777-6655",
                "Alameda Ipe, 210",
                "Acesso lateral pela guarita 2",
                ResidentStatus.ACTIVE,
                encodeResidentPin(null, "(11) 99777-6655", false),
                encodeResidentPin(null, "(11) 99777-6655", true)
        ));

        Vehicle alpha = vehicleRepository.save(new Vehicle("ABC1D23", "Renault Duster", 48241, 49000, VehicleStatus.IN_OPERATION, LocalDate.now().plusMonths(7), LocalDate.now().plusMonths(7), LocalDate.now().plusMonths(10), LocalDate.now().minusMonths(2), "Manutencao preventiva realizada na ultima troca de oleo"));
        Vehicle beta = vehicleRepository.save(new Vehicle("FGH4J56", "Chevrolet Spin", 61120, 62000, VehicleStatus.AVAILABLE, LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(6), LocalDate.now().minusMonths(1), "Verificar desgaste de pneus no proximo ciclo"));

        vehicleMaintenanceRecordRepository.save(new VehicleMaintenanceRecord(
                beta.getId(),
                beta.getPlate(),
                generateMaintenanceCode(beta),
                VehicleMaintenanceType.PREVENTIVE,
                VehicleMaintenancePriority.LOW,
                VehicleMaintenanceStatus.COMPLETED,
                OffsetDateTime.now().minusDays(10),
                OffsetDateTime.now().minusDays(9),
                LocalDate.now().minusDays(9),
                LocalDate.now().plusMonths(6),
                60500L,
                62000L,
                new java.math.BigDecimal("850.00"),
                "Oficina Central",
                "Servico concluido sem pendencias.",
                "Troca de oleo, filtros e alinhamento preventivo.",
                true
        ));

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
                carlos.getId(),
                carlos.getFullName(),
                alpha.getId(),
                alpha.getPlate(),
                OffsetDateTime.now().minusMinutes(8),
                null,
                null,
                "Equipe acionada e a caminho.",
                null,
                null
        ));
        incidentRepository.save(new Incident(
                com.seguranca.plataforma.operations.model.IncidentType.ESCORT,
                com.seguranca.plataforma.operations.model.IncidentPriority.MEDIUM,
                IncidentStatus.OPEN,
                bruno.getFullName(),
                bruno.getAddress(),
                OffsetDateTime.now().minusMinutes(3),
                marina.getId(),
                marina.getFullName(),
                beta.getId(),
                beta.getPlate(),
                null,
                null,
                null,
                null,
                null,
                null
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
        Agent savedAgent = agentRepository.save(agent);
        recordAudit(AuditActionType.CREATE, "Agent", savedAgent.getId(), "Cadastro de agente " + savedAgent.getFullName());
        return savedAgent;
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
        Agent savedAgent = agentRepository.save(agent);
        recordAudit(AuditActionType.UPDATE, "Agent", savedAgent.getId(), "Atualizacao do agente " + savedAgent.getFullName());
        return savedAgent;
    }

    @Transactional
    public void deleteAgent(Long id) {
        Agent agent = getAgent(id);
        agentRepository.delete(agent);
        recordAudit(AuditActionType.DELETE, "Agent", id, "Exclusao do agente " + agent.getFullName());
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listVehicles() {
        return vehicleRepository.findAll().stream()
                .sorted(Comparator.comparing(Vehicle::getId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleMaintenanceRecord> listVehicleMaintenanceRecords() {
        return vehicleMaintenanceRecordRepository.findAll().stream()
                .sorted(Comparator.comparing(VehicleMaintenanceRecord::getOpenedAt).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleMaintenanceOrderResponse> listVehicleMaintenanceOrders() {
        // Expõe a frota em formato de ordem de servico legivel para relatorios e paines externos.
        List<Vehicle> vehicles = listVehicles();
        Map<Long, Vehicle> vehiclesById = vehicles.stream().collect(java.util.stream.Collectors.toMap(Vehicle::getId, vehicle -> vehicle));
        return listVehicleMaintenanceRecords().stream()
                .map(record -> vehicleFleetReportCalculator.toOrderResponse(record, vehiclesById.get(record.getVehicleId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public FleetOperationalReportResponse fleetReport() {
        // Consolida o relatorio operacional da frota sem misturar com o resumo geral do dashboard.
        return vehicleFleetReportCalculator.buildReport(listVehicles(), listVehicleMaintenanceRecords());
    }

    @Transactional(readOnly = true)
    public List<AuditRecord> listRecentAuditRecords() {
        return auditRecordRepository.findTop10ByOrderByOccurredAtDesc();
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
                ResidentStatus.ACTIVE,
                encodeResidentPin(request.accessPin(), request.phoneNumber(), false),
                encodeResidentPin(request.coercionPin(), request.phoneNumber(), true)
        );
        Resident savedResident = residentRepository.save(resident);
        recordAudit(AuditActionType.CREATE, "Resident", savedResident.getId(), "Cadastro do morador " + savedResident.getFullName());
        return savedResident;
    }

    @Transactional
    public Resident updateResident(Long id, UpdateResidentRequest request) {
        Resident resident = getResident(id);
        resident.update(
                request.fullName(),
                request.phoneNumber(),
                request.address(),
                request.referenceNote(),
                request.status(),
                encodeResidentPin(request.accessPin(), request.phoneNumber(), false),
                encodeResidentPin(request.coercionPin(), request.phoneNumber(), true)
        );
        Resident savedResident = residentRepository.save(resident);
        recordAudit(AuditActionType.UPDATE, "Resident", savedResident.getId(), "Atualizacao do morador " + savedResident.getFullName());
        return savedResident;
    }

    @Transactional
    public void deleteResident(Long id) {
        Resident resident = getResident(id);
        residentRepository.delete(resident);
        recordAudit(AuditActionType.DELETE, "Resident", id, "Exclusao do morador " + resident.getFullName());
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
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        recordAudit(AuditActionType.CREATE, "Vehicle", savedVehicle.getId(), "Cadastro da viatura " + savedVehicle.getPlate());
        return savedVehicle;
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
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        recordAudit(AuditActionType.UPDATE, "Vehicle", savedVehicle.getId(), "Atualizacao da viatura " + savedVehicle.getPlate());
        return savedVehicle;
    }

    @Transactional
    public VehicleMaintenanceRecord addVehicleMaintenance(Long vehicleId, CreateVehicleMaintenanceRequest request) {
        // Registra manutencao com historico, custo e efeito operacional na viatura.
        Vehicle vehicle = getVehicle(vehicleId);
        validateVehicleMaintenanceRequest(request.kmAtService(), request.nextMaintenanceKm(), request.costAmount(), request.serviceDate(), request.dueDate(), request.status(), request.resolved());

        VehicleMaintenanceRecord record = new VehicleMaintenanceRecord(
                vehicle.getId(),
                vehicle.getPlate(),
                generateMaintenanceCode(vehicle),
                request.type(),
                request.priority(),
                request.status(),
                OffsetDateTime.now(ZoneOffset.UTC),
                resolveMaintenanceCompletedAt(request.status(), request.resolved()),
                request.serviceDate(),
                request.dueDate(),
                request.kmAtService(),
                request.nextMaintenanceKm(),
                request.costAmount(),
                request.supplierName(),
                normalizeOptionalText(request.resolutionNotes(), 1000),
                request.description().trim(),
                isMaintenanceResolved(request.status(), request.resolved())
        );

        applyMaintenanceImpact(
                vehicle,
                request.type(),
                request.status(),
                request.serviceDate(),
                request.kmAtService(),
                request.nextMaintenanceKm(),
                request.description(),
                isMaintenanceResolved(request.status(), request.resolved())
        );
        vehicleRepository.save(vehicle);
        VehicleMaintenanceRecord savedRecord = vehicleMaintenanceRecordRepository.save(record);
        recordAudit(AuditActionType.MAINTENANCE, "VehicleMaintenance", savedRecord.getId(), "Registro de manutencao " + savedRecord.getType() + " para a viatura " + savedRecord.getVehiclePlate());
        return savedRecord;
    }

    @Transactional
    public VehicleMaintenanceRecord updateVehicleMaintenance(Long maintenanceId, UpdateVehicleMaintenanceRequest request) {
        VehicleMaintenanceRecord record = getVehicleMaintenanceRecord(maintenanceId);
        Vehicle vehicle = getVehicle(record.getVehicleId());
        validateVehicleMaintenanceRequest(request.kmAtService(), request.nextMaintenanceKm(), request.costAmount(), request.serviceDate(), request.dueDate(), request.status(), request.resolved());

        record.update(
                request.type(),
                request.priority(),
                request.status(),
                resolveMaintenanceCompletedAt(request.status(), request.resolved()),
                request.serviceDate(),
                request.dueDate(),
                request.kmAtService(),
                request.nextMaintenanceKm(),
                request.costAmount(),
                request.supplierName(),
                normalizeOptionalText(request.resolutionNotes(), 1000),
                request.description().trim(),
                isMaintenanceResolved(request.status(), request.resolved())
        );

        applyMaintenanceImpact(
                vehicle,
                request.type(),
                request.status(),
                request.serviceDate(),
                request.kmAtService(),
                request.nextMaintenanceKm(),
                request.description(),
                isMaintenanceResolved(request.status(), request.resolved())
        );
        vehicleRepository.save(vehicle);
        VehicleMaintenanceRecord savedRecord = vehicleMaintenanceRecordRepository.save(record);
        recordAudit(AuditActionType.MAINTENANCE, "VehicleMaintenance", savedRecord.getId(), "Atualizacao da manutencao " + savedRecord.getType() + " da viatura " + savedRecord.getVehiclePlate());
        return savedRecord;
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicle(id);
        vehicleRepository.delete(vehicle);
        recordAudit(AuditActionType.DELETE, "Vehicle", id, "Exclusao da viatura " + vehicle.getPlate());
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
        validateVehicleOperationalReadiness(vehicle, request.scheduledStartAt(), request.scheduledEndAt(), null);

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
        Shift savedShift = shiftRepository.save(shift);
        recordAudit(AuditActionType.CREATE, "Shift", savedShift.getId(), "Criacao do turno para " + savedShift.getAgentName() + " na viatura " + savedShift.getVehiclePlate());
        return savedShift;
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
        if (request.status() != ShiftStatus.CLOSED || shift.getCheckInAt() == null) {
            // So bloqueia a alocacao de viatura irregular antes de abrir/reatribuir o turno.
            validateVehicleOperationalReadiness(vehicle, request.scheduledStartAt(), request.scheduledEndAt(), shift.getId());
        }

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

        Shift savedShift = shiftRepository.save(shift);
        recordAudit(AuditActionType.UPDATE, "Shift", savedShift.getId(), "Atualizacao do turno " + savedShift.getId() + " com status " + savedShift.getStatus());
        return savedShift;
    }

    @Transactional
    public Shift requestShiftHandoff(Long id, RequestShiftHandoffRequest request) {
        // Abre um pedido formal de troca de turno e deixa a transferencia pendente ate o aceite.
        Shift shift = getShift(id);
        enforceHandoffRequesterAuthorization(shift);
        if (shift.getAgentId().equals(request.toAgentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O agente que assume precisa ser diferente do agente atual.");
        }

        Agent newAgent = getAgent(request.toAgentId());
        Vehicle vehicle = getVehicle(shift.getVehicleId());
        activateShiftIfNeeded(shift, vehicle);
        shift.requestHandoff(
                shift.getAgentId(),
                shift.getAgentName(),
                newAgent.getId(),
                newAgent.getFullName(),
                OffsetDateTime.now(ZoneOffset.UTC),
                resolveCurrentActorUsername(),
                normalizeOptionalText(request.notes(), 500)
        );

        Shift savedShift = shiftRepository.save(shift);
        recordAudit(AuditActionType.HANDOFF, "Shift", savedShift.getId(), "Pedido de troca do agente " + savedShift.getHandoffFromAgentName() + " para " + savedShift.getHandoffToAgentName());
        return savedShift;
    }

    @Transactional
    public Shift acceptShiftHandoff(Long id, RespondShiftHandoffRequest request) {
        // Conclui a troca apenas quando o vigilante de destino aceita formalmente assumir o turno.
        Shift shift = getShift(id);
        if (shift.getStatus() != ShiftStatus.HANDOFF_PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao existe troca pendente para este turno.");
        }
        Long currentAgentId = resolveCurrentOperationalAgentId(true);
        if (!currentAgentId.equals(shift.getHandoffToAgentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o vigilante designado pode aceitar esta troca.");
        }

        shift.acceptHandoff(OffsetDateTime.now(ZoneOffset.UTC), normalizeOptionalText(request.notes(), 500));
        Shift savedShift = shiftRepository.save(shift);
        recordAudit(AuditActionType.HANDOFF, "Shift", savedShift.getId(), "Aceite da troca para o agente " + savedShift.getHandoffToAgentName());
        return savedShift;
    }

    @Transactional
    public Shift rejectShiftHandoff(Long id, RespondShiftHandoffRequest request) {
        // Permite recusar a troca sem perder a trilha de quem recusou e por qual motivo.
        Shift shift = getShift(id);
        if (shift.getStatus() != ShiftStatus.HANDOFF_PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao existe troca pendente para este turno.");
        }
        Long currentAgentId = resolveCurrentOperationalAgentId(true);
        if (!currentAgentId.equals(shift.getHandoffToAgentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o vigilante designado pode recusar esta troca.");
        }

        shift.rejectHandoff(
                OffsetDateTime.now(ZoneOffset.UTC),
                resolveCurrentActorUsername(),
                normalizeOptionalText(request.rejectionReason(), 500)
        );
        Shift savedShift = shiftRepository.save(shift);
        recordAudit(AuditActionType.HANDOFF, "Shift", savedShift.getId(), "Recusa da troca pelo agente " + shift.getHandoffToAgentName());
        return savedShift;
    }

    @Transactional
    public Shift superviseShift(Long id, SuperviseShiftRequest request) {
        // Consolida a acao de supervisao sobre presenca: atraso, falta, cobertura e normalizacao.
        Shift shift = getShift(id);
        ShiftSupervisionAction action = request.action();
        if (action == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a acao de supervisao do turno.");
        }

        switch (action) {
            case MARK_ON_TIME -> {
                shift.setAttendanceManually(ShiftAttendanceStatus.ON_TIME, 0, normalizeOptionalText(request.notes(), 500));
                if (shift.getStatus() == ShiftStatus.PLANNED) {
                    shift.update(
                            shift.getAgentId(),
                            shift.getAgentName(),
                            shift.getVehicleId(),
                            shift.getVehiclePlate(),
                            ShiftStatus.ACTIVE,
                            shift.getScheduledStartAt(),
                            shift.getScheduledEndAt(),
                            shift.getEndKm(),
                            shift.getFuelLevelPercent(),
                            shift.isTiresChecked(),
                            shift.isLightsChecked(),
                            shift.isDocumentsChecked(),
                            shift.getChecklistNotes()
                    );
                }
            }
            case MARK_LATE -> {
                int lateMinutes = request.lateMinutes() == null || request.lateMinutes() <= 0 ? 1 : request.lateMinutes();
                shift.setAttendanceManually(ShiftAttendanceStatus.LATE, lateMinutes, normalizeOptionalText(request.notes(), 500));
            }
            case MARK_ABSENT -> {
                shift.setAttendanceManually(ShiftAttendanceStatus.ABSENT, null, normalizeOptionalText(request.notes(), 500));
            }
            case APPLY_COVERAGE -> {
                if (request.replacementAgentId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o agente de cobertura.");
                }
                Agent replacementAgent = getAgent(request.replacementAgentId());
                if (replacementAgent.getId().equals(shift.getAgentId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O agente de cobertura precisa ser diferente do agente ausente.");
                }
                shift.applyCoverage(replacementAgent.getId(), replacementAgent.getFullName(), normalizeOptionalText(request.notes(), 500));
            }
            case CLEAR_COVERAGE -> applyAttendanceState(shift, ShiftAttendanceStatus.PENDING, null, normalizeOptionalText(request.notes(), 500));
        }

        Shift savedShift = shiftRepository.save(shift);
        recordAudit(AuditActionType.UPDATE, "Shift", savedShift.getId(), "Supervisao do turno " + savedShift.getId() + " com acao " + action.name());
        return savedShift;
    }

    @Transactional
    public void deleteShift(Long id) {
        Shift shift = getShift(id);
        shiftRepository.delete(shift);
        recordAudit(AuditActionType.DELETE, "Shift", id, "Exclusao do turno " + shift.getId());
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
        ShiftTelemetry savedTelemetry = shiftTelemetryRepository.save(telemetry);
        recordAudit(AuditActionType.TELEMETRY, "ShiftTelemetry", shiftId, "Sincronizacao de telemetria do turno " + shiftId);
        return savedTelemetry;
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

    @Transactional(readOnly = true)
    public List<IncidentEvidenceResponse> listIncidentEvidence() {
        return incidentEvidenceRepository.findAllByDeletedAtIsNullOrderByUploadedAtDesc().stream()
                .map(this::toIncidentEvidenceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentEvidenceResponse> listIncidentEvidenceByIncident(Long incidentId) {
        getIncident(incidentId);
        return incidentEvidenceRepository.findByIncidentIdAndDeletedAtIsNullOrderByUploadedAtDesc(incidentId).stream()
                .map(this::toIncidentEvidenceResponse)
                .toList();
    }

    @Transactional
    public Incident addIncident(CreateIncidentRequest request) {
        Resident resident = resolveResident(request.residentId(), request.residentName(), request.address());
        Long assignedAgentId = request.assignedAgentId();
        String assignedAgentName = assignedAgentId == null ? null : getAgent(assignedAgentId).getFullName();
        Long vehicleId = request.vehicleId();
        String vehiclePlate = vehicleId == null ? null : getVehicle(vehicleId).getPlate();

        Incident incident = new Incident(
                request.type(),
                request.priority(),
                IncidentStatus.OPEN,
                resident != null ? resident.getFullName() : request.residentName().trim(),
                resident != null ? resident.getAddress() : request.address().trim(),
                OffsetDateTime.now(),
                assignedAgentId,
                assignedAgentName,
                vehicleId,
                vehiclePlate,
                null,
                null,
                null,
                null,
                null,
                null
        );
        Incident savedIncident = incidentRepository.save(incident);
        recordAudit(AuditActionType.CREATE, "Incident", savedIncident.getId(), "Abertura da ocorrencia para " + savedIncident.getResidentName());
        appUserPushNotificationService.notifyIncidentWorkflowUpdated(savedIncident);
        return savedIncident;
    }

    @Transactional
    public Incident updateIncident(Long id, UpdateIncidentRequest request) {
        Incident incident = getIncident(id);
        Resident resident = resolveResident(request.residentId(), request.residentName(), request.address());
        Long assignedAgentId = request.assignedAgentId();
        String assignedAgentName = assignedAgentId == null ? null : getAgent(assignedAgentId).getFullName();
        Long vehicleId = request.vehicleId();
        String vehiclePlate = vehicleId == null ? null : getVehicle(vehicleId).getPlate();

        validateIncidentTransition(incident.getStatus(), request.status());

        incident.update(
                request.type(),
                request.priority(),
                request.status(),
                resident != null ? resident.getFullName() : request.residentName().trim(),
                resident != null ? resident.getAddress() : request.address().trim(),
                assignedAgentId,
                assignedAgentName,
                vehicleId,
                vehiclePlate
        );
        Incident savedIncident = incidentRepository.save(incident);
        recordAudit(AuditActionType.INCIDENT_WORKFLOW, "Incident", savedIncident.getId(), "Atualizacao da ocorrencia " + savedIncident.getId() + " para status " + savedIncident.getStatus());
        return savedIncident;
    }

    @Transactional
    public void deleteIncident(Long id) {
        Incident incident = getIncident(id);
        // Remove os arquivos fisicos antes de apagar a ocorrencia para nao deixar evidencia solta no disco.
        deleteIncidentEvidenceFiles(id);
        incidentRepository.delete(incident);
        recordAudit(AuditActionType.DELETE, "Incident", id, "Exclusao da ocorrencia " + incident.getId());
    }

    @Transactional
    public Incident dispatchIncident(Long id, DispatchIncidentRequest request) {
        // Formaliza o despacho da equipe para a ocorrencia com trilha de quem foi enviado.
        Incident incident = getIncident(id);
        ensureIncidentCanBeDispatched(incident);

        Agent agent = getAgent(request.assignedAgentId());
        Vehicle vehicle = getVehicle(request.vehicleId());
        ensureAgentEligibleForDispatch(agent);
        ensureVehicleEligibleForDispatch(vehicle);

        incident.dispatch(
                agent.getId(),
                agent.getFullName(),
                vehicle.getId(),
                vehicle.getPlate(),
                OffsetDateTime.now(ZoneOffset.UTC),
                request.dispatchNotes() != null ? request.dispatchNotes().trim() : null
        );
        Incident savedIncident = incidentRepository.save(incident);
        recordAudit(AuditActionType.INCIDENT_WORKFLOW, "Incident", savedIncident.getId(), "Despacho da ocorrencia " + savedIncident.getId() + " para " + savedIncident.getAssignedAgentName());
        appUserPushNotificationService.notifyIncidentWorkflowUpdated(savedIncident);
        return savedIncident;
    }

    @Transactional
    public Incident dispatchIncidentForCurrentRonda(Long id, RondaDispatchIncidentRequest request) {
        // A ronda assume e despacha a ocorrencia usando o proprio vinculo autenticado e a viatura do turno ativo.
        Long agentId = resolveCurrentOperationalAgentId(true);
        Incident incident = getIncident(id);
        ensureIncidentCanBeDispatched(incident);
        ensureIncidentNotAssignedToAnotherAgent(incident, agentId);
        ensureAgentHasNoOtherActiveIncident(agentId, id);

        Shift activeShift = resolveCurrentOperationalShift(agentId);
        Agent agent = getAgent(agentId);
        Vehicle vehicle = getVehicle(activeShift.getVehicleId());
        ensureAgentEligibleForDispatch(agent);
        ensureVehicleEligibleForDispatch(vehicle);

        incident.dispatch(
                agent.getId(),
                agent.getFullName(),
                vehicle.getId(),
                vehicle.getPlate(),
                OffsetDateTime.now(ZoneOffset.UTC),
                normalizeOptionalText(request == null ? null : request.dispatchNotes(), 500)
        );
        Incident savedIncident = incidentRepository.save(incident);
        recordAudit(AuditActionType.INCIDENT_WORKFLOW, "Incident", savedIncident.getId(), "Despacho mobile da ocorrencia " + savedIncident.getId() + " pela ronda " + savedIncident.getAssignedAgentName());
        appUserPushNotificationService.notifyIncidentWorkflowUpdated(savedIncident);
        return savedIncident;
    }

    @Transactional
    public Incident markIncidentOnSite(Long id, OnSiteIncidentRequest request) {
        // Registra a chegada da equipe no local da ocorrencia.
        Incident incident = getIncident(id);
        if (incident.getStatus() != IncidentStatus.DISPATCHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ocorrencia precisa estar despachada para registrar chegada.");
        }

        incident.markOnSite(
                OffsetDateTime.now(ZoneOffset.UTC),
                request.arrivalNotes() != null ? request.arrivalNotes().trim() : null
        );
        Incident savedIncident = incidentRepository.save(incident);
        recordAudit(AuditActionType.INCIDENT_WORKFLOW, "Incident", savedIncident.getId(), "Chegada ao local da ocorrencia " + savedIncident.getId());
        appUserPushNotificationService.notifyIncidentWorkflowUpdated(savedIncident);
        return savedIncident;
    }

    @Transactional
    public Incident markIncidentOnSiteForCurrentRonda(Long id, OnSiteIncidentRequest request) {
        // A chegada no local so pode ser confirmada pela ronda responsavel por aquela ocorrencia.
        Long agentId = resolveCurrentOperationalAgentId(true);
        Incident incident = getIncident(id);
        ensureIncidentBelongsToCurrentRonda(incident, agentId);
        if (incident.getStatus() != IncidentStatus.DISPATCHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ocorrencia precisa estar despachada para registrar chegada.");
        }

        incident.markOnSite(
                OffsetDateTime.now(ZoneOffset.UTC),
                normalizeOptionalText(request == null ? null : request.arrivalNotes(), 500)
        );
        Incident savedIncident = incidentRepository.save(incident);
        recordAudit(AuditActionType.INCIDENT_WORKFLOW, "Incident", savedIncident.getId(), "Chegada mobile ao local da ocorrencia " + savedIncident.getId());
        appUserPushNotificationService.notifyIncidentWorkflowUpdated(savedIncident);
        return savedIncident;
    }

    @Transactional
    public Incident closeIncident(Long id, CloseIncidentRequest request) {
        // Encerra a ocorrencia quando o atendimento foi concluido e documentado.
        Incident incident = getIncident(id);
        if (incident.getStatus() != IncidentStatus.ON_SITE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ocorrencia precisa estar no local antes do encerramento.");
        }

        incident.close(
                OffsetDateTime.now(ZoneOffset.UTC),
                normalizeOptionalText(request.closureNotes(), 1000)
        );
        Incident savedIncident = incidentRepository.save(incident);
        recordAudit(AuditActionType.INCIDENT_WORKFLOW, "Incident", savedIncident.getId(), "Encerramento da ocorrencia " + savedIncident.getId());
        appUserPushNotificationService.notifyIncidentWorkflowUpdated(savedIncident);
        return savedIncident;
    }

    @Transactional
    public Incident closeIncidentForCurrentRonda(Long id, RondaCloseIncidentRequest request) {
        // O encerramento mobile exige que a ronda esteja vinculada a ocorrencia e registre observacao final.
        Long agentId = resolveCurrentOperationalAgentId(true);
        Incident incident = getIncident(id);
        ensureIncidentBelongsToCurrentRonda(incident, agentId);
        if (incident.getStatus() != IncidentStatus.ON_SITE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ocorrencia precisa estar no local antes do encerramento.");
        }

        incident.close(
                OffsetDateTime.now(ZoneOffset.UTC),
                normalizeRequiredText(request.closureNotes(), "Informe a observacao final do atendimento.", 1000)
        );
        Incident savedIncident = incidentRepository.save(incident);
        recordAudit(AuditActionType.INCIDENT_WORKFLOW, "Incident", savedIncident.getId(), "Encerramento mobile da ocorrencia " + savedIncident.getId());
        appUserPushNotificationService.notifyIncidentWorkflowUpdated(savedIncident);
        return savedIncident;
    }

    @Transactional
    public IncidentEvidenceResponse addIncidentEvidence(Long incidentId, MultipartFile file, String notes) {
        // Persiste metadados e arquivo fisico da evidencia ligada a uma ocorrencia.
        Incident incident = getIncident(incidentId);
        validateEvidenceFile(file);

        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "-" + sanitizedFilename;
        Path incidentDirectory = storageRoot.resolve("incidents").resolve(String.valueOf(incidentId));
        Path destination = incidentDirectory.resolve(storedFilename);

        try {
            Files.createDirectories(incidentDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nao foi possivel armazenar a evidencia.");
        }

        IncidentEvidence evidence = new IncidentEvidence(
                incidentId,
                sanitizedFilename,
                storedFilename,
                StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream",
                file.getSize(),
                normalizeOptionalText(notes, 1000),
                resolveCurrentActorUsername(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        IncidentEvidence savedEvidence = incidentEvidenceRepository.save(evidence);
        recordAudit(AuditActionType.UPDATE, "IncidentEvidence", savedEvidence.getId(), "Envio de evidencia para a ocorrencia " + incident.getId());
        return toIncidentEvidenceResponse(savedEvidence);
    }

    @Transactional
    public IncidentEvidenceResponse deleteIncidentEvidence(Long incidentId, Long evidenceId, String reason) {
        // A exclusao e controlada: apaga o arquivo fisico, mas preserva trilha de quem removeu e por qual motivo.
        IncidentEvidence evidence = incidentEvidenceRepository.findByIdAndIncidentIdAndDeletedAtIsNull(evidenceId, incidentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidencia da ocorrencia nao encontrada."));

        if (evidence.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A evidencia ja foi removida anteriormente.");
        }

        Path evidencePath = storageRoot
                .resolve("incidents")
                .resolve(String.valueOf(incidentId))
                .resolve(evidence.getStoredFilename());

        try {
            Files.deleteIfExists(evidencePath);
            pruneIncidentDirectory(evidencePath.getParent());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nao foi possivel remover o arquivo da evidencia.");
        }

        evidence.markDeleted(
                OffsetDateTime.now(ZoneOffset.UTC),
                resolveCurrentActorUsername(),
                normalizeOptionalText(reason, 1000)
        );
        IncidentEvidence savedEvidence = incidentEvidenceRepository.save(evidence);
        recordAudit(AuditActionType.DELETE, "IncidentEvidence", savedEvidence.getId(), "Exclusao controlada da evidencia da ocorrencia " + incidentId);
        return toIncidentEvidenceResponse(savedEvidence);
    }

    @Transactional(readOnly = true)
    public IncidentEvidenceDownload downloadIncidentEvidence(Long incidentId, Long evidenceId) {
        IncidentEvidence evidence = incidentEvidenceRepository.findByIdAndIncidentIdAndDeletedAtIsNull(evidenceId, incidentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidencia da ocorrencia nao encontrada."));

        if (evidence.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.GONE, "A evidencia foi removida e nao pode mais ser baixada.");
        }

        Path resourcePath = storageRoot.resolve("incidents").resolve(String.valueOf(incidentId)).resolve(evidence.getStoredFilename());
        Resource resource = new FileSystemResource(resourcePath);

        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo fisico da evidencia nao encontrado.");
        }

        return new IncidentEvidenceDownload(
                resource,
                evidence.getOriginalFilename(),
                StringUtils.hasText(evidence.getContentType()) ? evidence.getContentType() : "application/octet-stream"
        );
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        // Consolida o estado operacional em uma unica resposta para reduzir chamadas do dashboard.
        List<Resident> residents = listResidents();
        List<Agent> agents = listAgents();
        List<Vehicle> vehicles = listVehicles();
        List<VehicleMaintenanceRecord> maintenanceRecords = listVehicleMaintenanceRecords();
        List<AuditRecord> auditRecords = listRecentAuditRecords();
        List<Shift> shifts = listShifts();
        List<Incident> incidents = listIncidents();

        long activeAgents = agents.stream()
                .filter(agent -> agent.getStatus() == AgentStatus.ACTIVE || agent.getStatus() == AgentStatus.ON_DUTY)
                .count();
        long availableVehicles = vehicles.stream()
                .filter(vehicle -> vehicle.getStatus() == VehicleStatus.AVAILABLE || vehicle.getStatus() == VehicleStatus.IN_OPERATION)
                .count();
        long activeShifts = shifts.stream()
                .filter(shift -> shift.getStatus() == ShiftStatus.ACTIVE || shift.getStatus() == ShiftStatus.HANDOFF || shift.getStatus() == ShiftStatus.HANDOFF_PENDING)
                .count();
        long lateShifts = shifts.stream()
                .filter(shift -> shift.getAttendanceStatus() == ShiftAttendanceStatus.LATE)
                .count();
        long absentShifts = shifts.stream()
                .filter(shift -> shift.getAttendanceStatus() == ShiftAttendanceStatus.ABSENT)
                .count();
        long openIncidents = incidents.stream()
                .filter(incident -> incident.getStatus() != IncidentStatus.CLOSED)
                .count();
        long maintenanceAlerts = vehicles.stream()
                .filter(this::hasVehicleAlert)
                .count();
        long openMaintenanceOrders = maintenanceRecords.stream()
                .filter(record -> record.getStatus() != VehicleMaintenanceStatus.COMPLETED && record.getStatus() != VehicleMaintenanceStatus.CANCELLED)
                .count();
        long criticalMaintenanceOrders = maintenanceRecords.stream()
                .filter(record -> record.getPriority() == VehicleMaintenancePriority.CRITICAL || record.getStatus() == VehicleMaintenanceStatus.WAITING_PARTS)
                .count();
        ActivePatrolResponse activePatrol = buildActivePatrol(agents, vehicles, shifts, incidents);

        return new DashboardSummaryResponse(
                residents.size(),
                agents.size(),
                activeAgents,
                availableVehicles,
                activeShifts,
                lateShifts,
                absentShifts,
                openIncidents,
                maintenanceAlerts,
                openMaintenanceOrders,
                criticalMaintenanceOrders,
                activePatrol,
                auditRecords,
                residents,
                agents,
                vehicles,
                maintenanceRecords,
                shifts,
                incidents
        );
    }

    @Transactional(readOnly = true)
    public ActivePatrolResponse activePatrolSummary() {
        // Reaproveita o mesmo recorte operacional do dashboard para web, mobile e visao autenticada do morador.
        return buildActivePatrol(listAgents(), listVehicles(), listShifts(), listIncidents());
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
                .filter(shift -> shift.getStatus() == ShiftStatus.ACTIVE || shift.getStatus() == ShiftStatus.HANDOFF || shift.getStatus() == ShiftStatus.HANDOFF_PENDING)
                .count();
        long openIncidents = incidents.stream()
                .filter(incident -> incident.getStatus() != IncidentStatus.CLOSED)
                .count();
        long maintenanceAlerts = vehicles.stream()
                .filter(this::hasVehicleAlert)
                .count();
        long openMaintenanceOrders = listVehicleMaintenanceRecords().stream()
                .filter(record -> record.getStatus() != VehicleMaintenanceStatus.COMPLETED && record.getStatus() != VehicleMaintenanceStatus.CANCELLED)
                .count();

        return new ClientPortalResponse(
                activeShifts,
                openIncidents,
                availableVehicles,
                maintenanceAlerts,
                openMaintenanceOrders,
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

    private VehicleMaintenanceRecord getVehicleMaintenanceRecord(Long id) {
        return vehicleMaintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de manutencao nao encontrado"));
    }

    private boolean hasVehicleAlert(Vehicle vehicle) {
        // Concentra os alertas de manutencao e vencimento documental da viatura no dashboard.
        boolean hasOpenCriticalOrder = vehicleMaintenanceRecordRepository.findByVehicleIdOrderByOpenedAtDesc(vehicle.getId()).stream()
                .anyMatch(record -> record.getStatus() != VehicleMaintenanceStatus.COMPLETED
                        && record.getStatus() != VehicleMaintenanceStatus.CANCELLED
                        && record.getPriority() == VehicleMaintenancePriority.CRITICAL);
        return vehicle.isMaintenanceDueSoon(1_000L) || hasOpenCriticalOrder || vehicle.hasDocumentAlert(LocalDate.now(), 30);
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

    private void validateVehicleOperationalReadiness(Vehicle vehicle, OffsetDateTime scheduledStartAt, OffsetDateTime scheduledEndAt, Long currentShiftId) {
        // Impede uso de viatura bloqueada, com documento vencido ou manutencao estourada em novos turnos.
        if (!vehicle.isOperationallyReady()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A viatura selecionada nao esta liberada para operacao.");
        }

        LocalDate referenceDate = scheduledStartAt.toLocalDate();
        if (vehicle.isMaintenanceDue()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A viatura selecionada esta com manutencao vencida e nao pode receber novo turno.");
        }

        if (vehicle.getIpvaExpiry() != null && vehicle.getIpvaExpiry().isBefore(referenceDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O IPVA da viatura selecionada estara vencido no inicio do turno.");
        }

        if (vehicle.getLicensingExpiry() != null && vehicle.getLicensingExpiry().isBefore(referenceDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O licenciamento da viatura selecionada estara vencido no inicio do turno.");
        }

        if (vehicle.getInsuranceExpiry() != null && vehicle.getInsuranceExpiry().isBefore(referenceDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O seguro da viatura selecionada estara vencido no inicio do turno.");
        }

        boolean hasOverlappingShift = shiftRepository.findAll().stream()
                .filter(existingShift -> currentShiftId == null || !existingShift.getId().equals(currentShiftId))
                .filter(existingShift -> existingShift.getVehicleId().equals(vehicle.getId()))
                .filter(existingShift -> existingShift.getStatus() != ShiftStatus.CLOSED)
                .anyMatch(existingShift ->
                        existingShift.getScheduledStartAt().isBefore(scheduledEndAt)
                                && scheduledStartAt.isBefore(existingShift.getScheduledEndAt()));

        if (hasOverlappingShift) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A viatura selecionada ja esta comprometida em outro turno nesse intervalo.");
        }
    }

    private void validateVehicleMaintenanceRequest(
            Long kmAtService,
            Long nextMaintenanceKm,
            java.math.BigDecimal costAmount,
            LocalDate plannedServiceDate,
            LocalDate dueDate,
            VehicleMaintenanceStatus status,
            boolean resolved
    ) {
        // Impede historico financeiro e de quilometragem inconsistente na ordem de servico.
        if (kmAtService != null && kmAtService < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quilometragem da manutencao nao pode ser negativa.");
        }

        if (nextMaintenanceKm != null && nextMaintenanceKm < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A proxima manutencao nao pode ter quilometragem negativa.");
        }

        if (costAmount != null && costAmount.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O custo da manutencao nao pode ser negativo.");
        }

        if (plannedServiceDate != null && dueDate != null && dueDate.isBefore(plannedServiceDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de vencimento da OS nao pode ser anterior a data do servico.");
        }

        if (resolved && status != VehicleMaintenanceStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma manutencao marcada como resolvida precisa estar com status concluido.");
        }
    }

    private void applyMaintenanceImpact(
            Vehicle vehicle,
            VehicleMaintenanceType type,
            VehicleMaintenanceStatus status,
            LocalDate serviceDate,
            Long kmAtService,
            Long nextMaintenanceKm,
            String description,
            boolean resolved
    ) {
        // Reflete o andamento da manutencao no cadastro principal da viatura.
        long updatedCurrentKm = kmAtService != null ? Math.max(vehicle.getCurrentKm(), kmAtService) : vehicle.getCurrentKm();
        long updatedNextMaintenanceKm = nextMaintenanceKm != null ? nextMaintenanceKm : vehicle.getNextMaintenanceKm();
        LocalDate updatedLastMaintenanceAt = serviceDate != null ? serviceDate : vehicle.getLastMaintenanceAt();
        String updatedNotes = StringUtils.hasText(description) ? description.trim() : vehicle.getMaintenanceNotes();
        VehicleStatus updatedStatus = vehicle.getStatus();

        if ((status == VehicleMaintenanceStatus.OPEN || status == VehicleMaintenanceStatus.IN_PROGRESS || status == VehicleMaintenanceStatus.WAITING_PARTS)
                && (type == VehicleMaintenanceType.CORRECTIVE || type == VehicleMaintenanceType.INSPECTION)) {
            updatedStatus = VehicleStatus.MAINTENANCE;
        } else if (resolved && vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            updatedStatus = VehicleStatus.AVAILABLE;
        }

        vehicle.update(
                vehicle.getPlate(),
                vehicle.getModel(),
                updatedCurrentKm,
                updatedNextMaintenanceKm,
                updatedStatus,
                vehicle.getIpvaExpiry(),
                vehicle.getLicensingExpiry(),
                vehicle.getInsuranceExpiry(),
                updatedLastMaintenanceAt,
                updatedNotes
        );
    }

    private String generateMaintenanceCode(Vehicle vehicle) {
        // Gera um identificador curto de OS que a operacao consegue citar por telefone, radio e relatorio.
        String prefix = vehicle.getPlate().replaceAll("[^A-Z0-9]", "").toUpperCase(Locale.ROOT);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return "OS-" + prefix + "-" + suffix;
    }

    private OffsetDateTime resolveMaintenanceCompletedAt(VehicleMaintenanceStatus status, boolean resolved) {
        // Centraliza a coerencia entre status da OS e carimbo de conclusao.
        if (status == VehicleMaintenanceStatus.COMPLETED || resolved) {
            return OffsetDateTime.now(ZoneOffset.UTC);
        }

        return null;
    }

    private boolean isMaintenanceResolved(VehicleMaintenanceStatus status, boolean resolved) {
        return status == VehicleMaintenanceStatus.COMPLETED || resolved;
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

        if (coveredAgent != null && coveredAgent.getId().equals(shift.getAgentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O vigilante de cobertura precisa ser diferente do vigilante coberto.");
        }
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

    private void ensureIncidentCanBeDispatched(Incident incident) {
        // O despacho parte apenas de ocorrencia aberta; estados posteriores exigem outro fluxo.
        if (incident.getStatus() == IncidentStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e possivel despachar uma ocorrencia encerrada.");
        }
        if (incident.getStatus() == IncidentStatus.DISPATCHED || incident.getStatus() == IncidentStatus.ON_SITE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ocorrencia ja esta em atendimento operacional.");
        }
    }

    private void ensureIncidentNotAssignedToAnotherAgent(Incident incident, Long currentAgentId) {
        // A ronda mobile nao pode sequestrar uma ocorrencia que ja foi atribuida para outro vigilante.
        if (incident.getAssignedAgentId() != null && !incident.getAssignedAgentId().equals(currentAgentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A ocorrencia ja esta atribuida a outro vigilante.");
        }
    }

    private void ensureIncidentBelongsToCurrentRonda(Incident incident, Long currentAgentId) {
        // Chegada e encerramento exigem que a conta autenticada represente a mesma ronda despachada.
        if (incident.getAssignedAgentId() == null || !incident.getAssignedAgentId().equals(currentAgentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A ocorrencia nao esta atribuida ao vigilante autenticado.");
        }
    }

    private void ensureAgentEligibleForDispatch(Agent agent) {
        // So vigias ativos ou em servico podem assumir uma ocorrencia.
        if (agent.getStatus() != AgentStatus.ACTIVE && agent.getStatus() != AgentStatus.ON_DUTY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O vigilante precisa estar ativo para assumir a ocorrencia.");
        }
    }

    private void ensureVehicleEligibleForDispatch(Vehicle vehicle) {
        // O despacho nao pode usar viatura bloqueada, em manutencao ou com documento vencido.
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || vehicle.getStatus() == VehicleStatus.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A viatura selecionada nao esta disponivel para despacho.");
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if ((vehicle.getIpvaExpiry() != null && vehicle.getIpvaExpiry().isBefore(today))
                || (vehicle.getLicensingExpiry() != null && vehicle.getLicensingExpiry().isBefore(today))
                || (vehicle.getInsuranceExpiry() != null && vehicle.getInsuranceExpiry().isBefore(today))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A viatura possui documentacao vencida e nao pode ser enviada para atendimento.");
        }
    }

    private void ensureAgentHasNoOtherActiveIncident(Long agentId, Long currentIncidentId) {
        // A mesma ronda nao deve assumir dois atendimentos simultaneos pelo mobile sem supervisao.
        boolean hasAnotherActiveIncident = incidentRepository.findAll().stream()
                .anyMatch(incident -> incident.getAssignedAgentId() != null
                        && incident.getAssignedAgentId().equals(agentId)
                        && !incident.getId().equals(currentIncidentId)
                        && incident.getStatus() != IncidentStatus.CLOSED);
        if (hasAnotherActiveIncident) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe outra ocorrencia em atendimento para este vigilante.");
        }
    }

    private Shift resolveCurrentOperationalShift(Long agentId) {
        // A ronda mobile despacha usando a viatura realmente vinculada ao turno ativo dela.
        return shiftRepository.findFirstByAgentIdAndStatusInOrderByStartedAtDesc(
                        agentId,
                        List.of(ShiftStatus.ACTIVE, ShiftStatus.HANDOFF, ShiftStatus.HANDOFF_PENDING)
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao existe turno operacional ativo vinculado ao vigilante autenticado."));
    }

    private void validateIncidentTransition(IncidentStatus currentStatus, IncidentStatus requestedStatus) {
        // Evita regressao de estado e saltos incoerentes no fluxo de atendimento.
        if (currentStatus == IncidentStatus.CLOSED && requestedStatus != IncidentStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ocorrencia encerrada nao pode voltar para outro status.");
        }

        if (currentStatus == IncidentStatus.OPEN && requestedStatus == IncidentStatus.ON_SITE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ocorrencia precisa ser despachada antes de chegar ao local.");
        }

        if (currentStatus == IncidentStatus.OPEN && requestedStatus == IncidentStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ocorrencia nao pode ser encerrada sem despacho.");
        }
    }

    private ActivePatrolResponse buildActivePatrol(List<Agent> agents, List<Vehicle> vehicles, List<Shift> shifts, List<Incident> incidents) {
        // Monta o cartao e o mapa da patrulha ativa com ultimo ponto e historico de telemetria.
        Shift activeShift = shifts.stream()
                .filter(shift -> shift.getStatus() == ShiftStatus.ACTIVE || shift.getStatus() == ShiftStatus.HANDOFF || shift.getStatus() == ShiftStatus.HANDOFF_PENDING)
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

    private void recordAudit(AuditActionType actionType, String entityName, Long entityId, String description) {
        // Registra a trilha minima das acoes criticas com o usuario autenticado quando houver contexto.
        AuditRecord record = new AuditRecord(
                actionType,
                entityName,
                entityId,
                resolveCurrentActorUsername(),
                OffsetDateTime.now(ZoneOffset.UTC),
                description
        );
        auditRecordRepository.save(record);
        operationsRealtimeService.publish(actionType.name(), entityName, entityId, description);
    }

    private IncidentEvidenceResponse toIncidentEvidenceResponse(IncidentEvidence evidence) {
        Incident incident = getIncident(evidence.getIncidentId());
        return new IncidentEvidenceResponse(
                evidence.getId(),
                evidence.getIncidentId(),
                incident.getResidentName(),
                evidence.getOriginalFilename(),
                evidence.getContentType(),
                evidence.getFileSizeBytes(),
                evidence.getNotes(),
                evidence.getUploadedBy(),
                evidence.getUploadedAt(),
                evidence.getUploadedAt().plusDays(retentionProperties.getIncidentEvidenceRetentionDays()),
                "/api/incidents/" + evidence.getIncidentId() + "/evidence/" + evidence.getId() + "/download"
        );
    }

    private void validateEvidenceFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um arquivo de evidencia.");
        }

        if (file.getSize() > 15L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A evidencia excede o limite de 15 MB.");
        }
    }

    private void initializeStorageDirectories() {
        try {
            Files.createDirectories(storageRoot.resolve("incidents"));
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel inicializar o diretorio de evidencias.", exception);
        }
    }

    private void deleteIncidentEvidenceFiles(Long incidentId) {
        // Faz a limpeza local do diretorio de anexos ligado a uma ocorrencia removida.
        List<IncidentEvidence> evidences = incidentEvidenceRepository.findByIncidentIdAndDeletedAtIsNullOrderByUploadedAtDesc(incidentId);
        for (IncidentEvidence evidence : evidences) {
            Path evidencePath = storageRoot
                    .resolve("incidents")
                    .resolve(String.valueOf(incidentId))
                    .resolve(evidence.getStoredFilename());
            try {
                Files.deleteIfExists(evidencePath);
            } catch (IOException exception) {
                // A limpeza e best effort; a varredura agendada de retencao remove sobras se algo falhar.
            }
        }

        Path incidentDirectory = storageRoot.resolve("incidents").resolve(String.valueOf(incidentId));
        try (var listing = Files.list(incidentDirectory)) {
            if (!listing.findAny().isPresent()) {
                Files.deleteIfExists(incidentDirectory);
            }
        } catch (IOException exception) {
            // O diretorio vazio nao precisa bloquear a exclusao funcional da ocorrencia.
        }
    }

    private String encodeResidentPin(String explicitPin, String phoneNumber, boolean coercionMode) {
        // O cadastro do morador passa a usar PIN dedicado para acesso e um PIN separado para coacao.
        String normalizedPin = normalizeResidentPin(explicitPin);
        if (normalizedPin == null) {
            normalizedPin = deriveResidentPinFromPhone(phoneNumber, coercionMode);
        }
        return passwordEncoder.encode(normalizedPin);
    }

    private String normalizeResidentPin(String pin) {
        if (!StringUtils.hasText(pin)) {
            return null;
        }

        String digitsOnly = pin.replaceAll("\\D+", "");
        if (digitsOnly.length() < 4 || digitsOnly.length() > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O PIN do morador deve ter entre 4 e 6 digitos.");
        }
        return digitsOnly;
    }

    private String deriveResidentPinFromPhone(String phoneNumber, boolean coercionMode) {
        String digitsOnly = phoneNumber == null ? "" : phoneNumber.replaceAll("\\D+", "");
        if (digitsOnly.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao foi possivel derivar o PIN padrao do morador a partir do telefone.");
        }

        String lastFourDigits = digitsOnly.substring(digitsOnly.length() - 4);
        return coercionMode ? new StringBuilder(lastFourDigits).reverse().toString() : lastFourDigits;
    }

    private void enforceHandoffRequesterAuthorization(Shift shift) {
        // Abertura da troca pode vir da ronda dona do turno ou de um perfil administrativo.
        AppUser currentUser = resolveCurrentAppUser();
        if (currentUser.getRole() != AppUserRole.RONDA) {
            return;
        }

        Long linkedAgentId = resolveCurrentOperationalAgentId(true);
        if (!linkedAgentId.equals(shift.getAgentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A conta autenticada nao representa o vigilante atual deste turno.");
        }
    }

    private Long resolveCurrentOperationalAgentId(boolean required) {
        // Acoes sensiveis da ronda precisam sair do usuario autenticado, nao de IDs enviados pelo cliente.
        AppUser currentUser = resolveCurrentAppUser();
        if (currentUser.getRole() != AppUserRole.RONDA) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente um usuario de ronda vinculado a vigilante pode executar esta acao.");
            }
            return null;
        }

        if (currentUser.getLinkedAgentId() == null && required) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A conta da ronda precisa estar vinculada a um vigilante para assumir ou recusar trocas.");
        }
        return currentUser.getLinkedAgentId();
    }

    private AppUser resolveCurrentAppUser() {
        String username = resolveCurrentActorUsername();
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado nao encontrado."));
    }

    private String sanitizeFilename(String originalFilename) {
        String fallback = "evidencia.bin";
        if (!StringUtils.hasText(originalFilename)) {
            return fallback;
        }

        String sanitized = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return StringUtils.hasText(sanitized) ? sanitized : fallback;
    }

    private void pruneIncidentDirectory(Path directory) {
        // Remove diretorios vazios deixados pela exclusao para nao acumular lixo no storage.
        if (directory == null) {
            return;
        }

        Path incidentsRoot = storageRoot.resolve("incidents");
        Path current = directory;
        while (current != null && current.startsWith(incidentsRoot) && !current.equals(incidentsRoot)) {
            try (var listing = Files.list(current)) {
                if (listing.findAny().isPresent()) {
                    return;
                }
            } catch (IOException exception) {
                return;
            }

            try {
                Files.deleteIfExists(current);
            } catch (IOException exception) {
                return;
            }

            current = current.getParent();
        }
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String normalized = value.trim();
        return normalized.length() > maxLength ? normalized.substring(0, maxLength) : normalized;
    }

    private String normalizeRequiredText(String value, String message, int maxLength) {
        // Fechamentos sensiveis exigem texto minimo para deixar trilha util de auditoria.
        String normalized = normalizeOptionalText(value, maxLength);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String resolveCurrentActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return "sistema";
        }

        return authentication.getName();
    }

    public record IncidentEvidenceDownload(
            Resource resource,
            String originalFilename,
            String contentType
    ) {
    }
}
