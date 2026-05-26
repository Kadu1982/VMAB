package com.seguranca.plataforma.hr.service;

import com.seguranca.plataforma.auth.AppUserRepository;
import com.seguranca.plataforma.hr.dto.CreateHrEmployeeRequest;
import com.seguranca.plataforma.hr.dto.HrAttendanceResponse;
import com.seguranca.plataforma.hr.dto.HrEmployeeAlertResponse;
import com.seguranca.plataforma.hr.dto.HrEmployeeResponse;
import com.seguranca.plataforma.hr.dto.HrSummaryResponse;
import com.seguranca.plataforma.hr.dto.RecordHrAttendanceRequest;
import com.seguranca.plataforma.hr.dto.UpdateHrEmployeeRequest;
import com.seguranca.plataforma.hr.model.HrAttendance;
import com.seguranca.plataforma.hr.model.HrAttendanceType;
import com.seguranca.plataforma.hr.model.HrEmployee;
import com.seguranca.plataforma.hr.model.HrEmployeeCategory;
import com.seguranca.plataforma.hr.model.HrEmployeeStatus;
import com.seguranca.plataforma.hr.repository.HrAttendanceRepository;
import com.seguranca.plataforma.hr.repository.HrEmployeeRepository;
import com.seguranca.plataforma.operations.model.Agent;
import com.seguranca.plataforma.operations.model.AgentStatus;
import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.repository.AgentRepository;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HrService {
    private static final int RENEWAL_WINDOW_DAYS = 30;

    private final HrEmployeeRepository hrEmployeeRepository;
    private final HrAttendanceRepository hrAttendanceRepository;
    private final AgentRepository agentRepository;
    private final AppUserRepository appUserRepository;
    private final AuditRecordRepository auditRecordRepository;

    public HrService(
            HrEmployeeRepository hrEmployeeRepository,
            HrAttendanceRepository hrAttendanceRepository,
            AgentRepository agentRepository,
            AppUserRepository appUserRepository,
            AuditRecordRepository auditRecordRepository
    ) {
        this.hrEmployeeRepository = hrEmployeeRepository;
        this.hrAttendanceRepository = hrAttendanceRepository;
        this.agentRepository = agentRepository;
        this.appUserRepository = appUserRepository;
        this.auditRecordRepository = auditRecordRepository;
    }

    @PostConstruct
    void bootstrapFromAgents() {
        synchronizeAllAgentsSilently();
    }

    @Transactional(readOnly = true)
    public List<HrEmployeeResponse> listEmployees() {
        return hrEmployeeRepository.findAllByOrderByIdAsc().stream()
                .map(this::toEmployeeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HrAttendanceResponse> listAttendance() {
        List<HrAttendance> recentAttendance = hrAttendanceRepository.findTop20ByOrderByOccurredAtDesc();
        Map<Long, String> employeeNames = loadEmployeeNames(recentAttendance.stream()
                .map(HrAttendance::getEmployeeId)
                .toList());
        return recentAttendance.stream()
                .map(attendance -> HrAttendanceResponse.fromEntity(attendance, employeeNames.get(attendance.getEmployeeId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public HrSummaryResponse summary() {
        List<HrEmployee> employees = hrEmployeeRepository.findAllByOrderByIdAsc();
        List<HrAttendance> attendance = hrAttendanceRepository.findTop20ByOrderByOccurredAtDesc();
        Map<Long, String> agentNames = loadAgentNames(employees.stream().map(HrEmployee::getLinkedAgentId).toList());
        Map<Long, String> appUserNames = loadAppUserNames(employees.stream().map(HrEmployee::getLinkedAppUserId).toList());

        List<HrEmployeeResponse> employeeResponses = employees.stream()
                .map(employee -> toEmployeeResponse(employee, agentNames, appUserNames))
                .toList();

        List<HrEmployeeAlertResponse> alerts = employees.stream()
                .flatMap(employee -> buildAlerts(employee).stream())
                .toList();

        List<HrAttendanceResponse> attendanceResponses = attendance.stream()
                .map(item -> HrAttendanceResponse.fromEntity(item, resolveEmployeeName(item.getEmployeeId(), employees)))
                .toList();

        long activeEmployees = employees.stream().filter(employee -> employee.getStatus() == HrEmployeeStatus.ACTIVE).count();
        long blockedEmployees = employees.stream().filter(employee -> employee.getStatus() == HrEmployeeStatus.BLOCKED || employee.getStatus() == HrEmployeeStatus.TERMINATED).count();
        long checkedInNowEmployees = employees.stream().filter(this::isCurrentlyCheckedIn).count();

        return new HrSummaryResponse(
                employees.size(),
                activeEmployees,
                blockedEmployees,
                checkedInNowEmployees,
                alerts.size(),
                employeeResponses,
                alerts,
                attendanceResponses
        );
    }

    @Transactional
    public HrEmployeeResponse createEmployee(CreateHrEmployeeRequest request) {
        validateUniqueEmployeeCode(request.employeeCode(), null);
        validateLinkedAgent(request.linkedAgentId(), null);
        validateLinkedAppUser(request.linkedAppUserId(), null);

        HrEmployee employee = new HrEmployee(
                normalizeEmployeeCode(request.employeeCode()),
                request.fullName().trim(),
                request.category(),
                request.status(),
                normalizeOptional(request.documentNumber()),
                normalizeOptional(request.address()),
                normalizeOptional(request.phoneNumber()),
                normalizeOptional(request.email()),
                normalizeOptional(request.photoUrl()),
                normalizeOptional(request.cnhCategory()),
                request.cnhExpiry(),
                request.medicalExamExpiry(),
                request.trainingExpiry(),
                normalizeOptional(request.trainingNotes()),
                normalizeOptional(request.documentNotes()),
                request.hireDate(),
                request.terminationDate(),
                request.linkedAgentId(),
                request.linkedAppUserId(),
                request.pointEnabled()
        );

        HrEmployee saved = hrEmployeeRepository.save(employee);
        recordAudit(AuditActionType.CREATE, "HrEmployee", saved.getId(), "Cadastro RH do funcionario " + saved.getFullName());
        return toEmployeeResponse(saved);
    }

    @Transactional
    public HrEmployeeResponse updateEmployee(Long id, UpdateHrEmployeeRequest request) {
        HrEmployee employee = getEmployee(id);
        validateUniqueEmployeeCode(request.employeeCode(), id);
        validateLinkedAgent(request.linkedAgentId(), id);
        validateLinkedAppUser(request.linkedAppUserId(), id);

        employee.updateProfile(
                normalizeEmployeeCode(request.employeeCode()),
                request.fullName().trim(),
                request.category(),
                request.status(),
                normalizeOptional(request.documentNumber()),
                normalizeOptional(request.address()),
                normalizeOptional(request.phoneNumber()),
                normalizeOptional(request.email()),
                normalizeOptional(request.photoUrl()),
                normalizeOptional(request.cnhCategory()),
                request.cnhExpiry(),
                request.medicalExamExpiry(),
                request.trainingExpiry(),
                normalizeOptional(request.trainingNotes()),
                normalizeOptional(request.documentNotes()),
                request.hireDate(),
                request.terminationDate(),
                request.linkedAgentId(),
                request.linkedAppUserId(),
                request.pointEnabled()
        );

        HrEmployee saved = hrEmployeeRepository.save(employee);
        recordAudit(AuditActionType.UPDATE, "HrEmployee", saved.getId(), "Atualizacao RH do funcionario " + saved.getFullName());
        return toEmployeeResponse(saved);
    }

    @Transactional
    public void terminateEmployee(Long id) {
        HrEmployee employee = getEmployee(id);
        employee.terminate();
        hrEmployeeRepository.save(employee);
        recordAudit(AuditActionType.DELETE, "HrEmployee", employee.getId(), "Encerramento RH do funcionario " + employee.getFullName());
    }

    @Transactional
    public HrEmployeeResponse syncAgent(Long agentId, Agent agent) {
        return upsertAgentSnapshot(agentId, agent, true);
    }

    @Transactional
    public void synchronizeAllAgentsSilently() {
        agentRepository.findAll().stream()
                .sorted(Comparator.comparing(Agent::getId))
                .forEach(agent -> upsertAgentSnapshot(agent.getId(), agent, false));
    }

    @Transactional
    public void archiveLinkedAgent(Agent agent) {
        hrEmployeeRepository.findByLinkedAgentId(agent.getId()).ifPresent(employee -> {
            employee.terminate();
            hrEmployeeRepository.save(employee);
            recordAudit(AuditActionType.DELETE, "HrEmployee", employee.getId(), "Agente removido do cadastro RH: " + employee.getFullName());
        });
    }

    private HrEmployeeResponse upsertAgentSnapshot(Long agentId, Agent agent, boolean recordAuditFlag) {
        HrEmployee employee = hrEmployeeRepository.findByLinkedAgentId(agentId)
                .orElseGet(() -> HrEmployee.fromAgent(agentId, agent));
        boolean isNew = employee.getId() == null;

        employee.applyAgentSnapshot(agent);
        HrEmployee saved = hrEmployeeRepository.save(employee);
        if (recordAuditFlag) {
            recordAudit(isNew ? AuditActionType.CREATE : AuditActionType.UPDATE, "HrEmployee", saved.getId(), "Sincronizacao do vigilante " + saved.getFullName() + " com RH");
        }
        return toEmployeeResponse(saved);
    }

    @Transactional
    public HrAttendanceResponse recordAttendance(Long employeeId, RecordHrAttendanceRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        ensurePointEnabled(employee);
        HrAttendance attendance = new HrAttendance(
                employeeId,
                request.eventType(),
                request.occurredAt(),
                normalizeOptional(request.deviceLabel()),
                request.latitude(),
                request.longitude(),
                normalizeOptional(request.note()),
                request.anomalyFlag(),
                normalizeOptional(request.anomalyReason())
        );

        if (request.eventType() == HrAttendanceType.CHECK_IN) {
            employee.recordCheckIn(request.occurredAt(), normalizeOptional(request.deviceLabel()), request.latitude(), request.longitude(), normalizeOptional(request.note()));
        } else {
            employee.recordCheckOut(request.occurredAt(), normalizeOptional(request.deviceLabel()), request.latitude(), request.longitude(), normalizeOptional(request.note()));
        }

        hrEmployeeRepository.save(employee);
        HrAttendance saved = hrAttendanceRepository.save(attendance);
        recordAudit(AuditActionType.TELEMETRY, "HrAttendance", saved.getId(), "Registro de ponto RH para " + employee.getFullName());
        return HrAttendanceResponse.fromEntity(saved, employee.getFullName());
    }

    @Transactional(readOnly = true)
    public List<HrEmployee> listEmployeeEntities() {
        return hrEmployeeRepository.findAllByOrderByIdAsc();
    }

    private HrEmployee getEmployee(Long id) {
        return hrEmployeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario nao encontrado."));
    }

    private HrEmployeeResponse toEmployeeResponse(HrEmployee employee) {
        Map<Long, String> agentNames = employee.getLinkedAgentId() == null
                ? Map.of()
                : loadAgentNames(List.of(employee.getLinkedAgentId()));
        Map<Long, String> appUserNames = employee.getLinkedAppUserId() == null
                ? Map.of()
                : loadAppUserNames(List.of(employee.getLinkedAppUserId()));
        return toEmployeeResponse(employee, agentNames, appUserNames);
    }

    private HrEmployeeResponse toEmployeeResponse(
            HrEmployee employee,
            Map<Long, String> agentNames,
            Map<Long, String> appUserNames
    ) {
        boolean cnhDue = isDueWithinWindow(employee.getCnhExpiry());
        boolean medicalDue = isDueWithinWindow(employee.getMedicalExamExpiry());
        boolean trainingDue = isDueWithinWindow(employee.getTrainingExpiry());
        return HrEmployeeResponse.fromEntity(
                employee,
                employee.getLinkedAgentId() != null ? agentNames.get(employee.getLinkedAgentId()) : null,
                employee.getLinkedAppUserId() != null ? appUserNames.get(employee.getLinkedAppUserId()) : null,
                cnhDue,
                medicalDue,
                trainingDue
        );
    }

    private List<HrEmployeeAlertResponse> buildAlerts(HrEmployee employee) {
        return java.util.stream.Stream.of(
                        buildAlert(employee, "CNH", employee.getCnhExpiry()),
                        buildAlert(employee, "EXAME_MEDICO", employee.getMedicalExamExpiry()),
                        buildAlert(employee, "TREINAMENTO", employee.getTrainingExpiry())
                )
                .filter(Objects::nonNull)
                .toList();
    }

    private HrEmployeeAlertResponse buildAlert(HrEmployee employee, String type, LocalDate dueDate) {
        if (dueDate == null) {
            return null;
        }
        long remaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(ZoneOffset.UTC), dueDate);
        if (remaining > RENEWAL_WINDOW_DAYS) {
            return null;
        }
        return new HrEmployeeAlertResponse(
                employee.getId(),
                employee.getFullName(),
                type,
                dueDate,
                remaining,
                "Vencimento de " + translateAlertType(type) + (remaining < 0 ? " atrasado" : " em " + remaining + " dia(s)")
        );
    }

    private boolean isDueWithinWindow(LocalDate dueDate) {
        if (dueDate == null) {
            return false;
        }
        long remaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(ZoneOffset.UTC), dueDate);
        return remaining <= RENEWAL_WINDOW_DAYS;
    }

    private boolean isCurrentlyCheckedIn(HrEmployee employee) {
        if (employee.getLastCheckInAt() == null) {
            return false;
        }
        if (employee.getLastCheckOutAt() == null) {
            return true;
        }
        return employee.getLastCheckInAt().isAfter(employee.getLastCheckOutAt());
    }

    private String resolveEmployeeName(Long employeeId, Collection<HrEmployee> employees) {
        if (employeeId == null) {
            return null;
        }
        return employees.stream()
                .filter(employee -> employee.getId().equals(employeeId))
                .map(HrEmployee::getFullName)
                .findFirst()
                .orElse(null);
    }

    private Map<Long, String> loadEmployeeNames(Collection<Long> employeeIds) {
        List<Long> ids = employeeIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return hrEmployeeRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(HrEmployee::getId, HrEmployee::getFullName, (left, right) -> left));
    }

    private Map<Long, String> loadAgentNames(Collection<Long> linkedAgentIds) {
        List<Long> ids = linkedAgentIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return agentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Agent::getId, Agent::getFullName, (left, right) -> left));
    }

    private Map<Long, String> loadAppUserNames(Collection<Long> linkedAppUserIds) {
        List<Long> ids = linkedAppUserIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return appUserRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(user -> user.getId(), user -> user.getUsername(), (left, right) -> left));
    }

    private void validateUniqueEmployeeCode(String employeeCode, Long currentId) {
        hrEmployeeRepository.findByEmployeeCodeIgnoreCase(normalizeEmployeeCode(employeeCode))
                .filter(employee -> currentId == null || !employee.getId().equals(currentId))
                .ifPresent(employee -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um funcionario com esse codigo.");
                });
    }

    private void validateLinkedAgent(Long linkedAgentId, Long currentId) {
        if (linkedAgentId == null) {
            return;
        }
        agentRepository.findById(linkedAgentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agente vinculado nao encontrado."));
        hrEmployeeRepository.findByLinkedAgentId(linkedAgentId)
                .filter(employee -> currentId == null || !employee.getId().equals(currentId))
                .ifPresent(employee -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse vigilante ja esta vinculado a outro funcionario RH.");
                });
    }

    private void validateLinkedAppUser(Long linkedAppUserId, Long currentId) {
        if (linkedAppUserId == null) {
            return;
        }
        appUserRepository.findById(linkedAppUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario vinculado nao encontrado."));
        hrEmployeeRepository.findAll().stream()
                .filter(employee -> linkedAppUserId.equals(employee.getLinkedAppUserId()))
                .filter(employee -> currentId == null || !employee.getId().equals(currentId))
                .findFirst()
                .ifPresent(employee -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse usuario ja esta vinculado a outro funcionario RH.");
                });
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeEmployeeCode(String employeeCode) {
        return employeeCode == null ? null : employeeCode.trim().toUpperCase();
    }

    private void ensurePointEnabled(HrEmployee employee) {
        if (employee.getStatus() != HrEmployeeStatus.ACTIVE || !employee.isPointEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ponto operacional esta indisponivel para este funcionario.");
        }
    }

    private String translateAlertType(String type) {
        return switch (type) {
            case "CNH" -> "CNH";
            case "EXAME_MEDICO" -> "exame medico";
            case "TREINAMENTO" -> "treinamento";
            default -> type.toLowerCase();
        };
    }

    private void recordAudit(AuditActionType actionType, String entityName, Long entityId, String description) {
        String actor = currentUsername();
        AuditRecord record = new AuditRecord(actionType, entityName, entityId, actor, OffsetDateTime.now(ZoneOffset.UTC), description);
        auditRecordRepository.save(record);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }
}
