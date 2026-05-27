package com.seguranca.plataforma.organization.service;

import com.seguranca.plataforma.hr.model.HrEmployee;
import com.seguranca.plataforma.hr.repository.HrEmployeeRepository;
import com.seguranca.plataforma.operations.model.AuditActionType;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.organization.dto.CreateBusinessSectorRequest;
import com.seguranca.plataforma.organization.dto.CreateBusinessUnitRequest;
import com.seguranca.plataforma.organization.dto.CreateHrEmployeeAssignmentRequest;
import com.seguranca.plataforma.organization.dto.CreateHrEmployeeDependentRequest;
import com.seguranca.plataforma.organization.dto.CreateResidentDependentRequest;
import com.seguranca.plataforma.organization.dto.CreateResidentVehicleRequest;
import com.seguranca.plataforma.organization.model.BusinessSector;
import com.seguranca.plataforma.organization.model.BusinessUnit;
import com.seguranca.plataforma.organization.model.BusinessUnitType;
import com.seguranca.plataforma.organization.model.FamilyRelationshipType;
import com.seguranca.plataforma.organization.model.HrEmployeeAssignment;
import com.seguranca.plataforma.organization.model.HrEmployeeDependent;
import com.seguranca.plataforma.organization.model.PersonDocument;
import com.seguranca.plataforma.organization.model.PersonDocumentOwnerType;
import com.seguranca.plataforma.organization.model.PersonDocumentType;
import com.seguranca.plataforma.organization.model.ResidentDependent;
import com.seguranca.plataforma.organization.model.ResidentVehicle;
import com.seguranca.plataforma.organization.repository.BusinessSectorRepository;
import com.seguranca.plataforma.organization.repository.BusinessUnitRepository;
import com.seguranca.plataforma.organization.repository.HrEmployeeAssignmentRepository;
import com.seguranca.plataforma.organization.repository.HrEmployeeDependentRepository;
import com.seguranca.plataforma.organization.repository.PersonDocumentRepository;
import com.seguranca.plataforma.organization.repository.ResidentDependentRepository;
import com.seguranca.plataforma.organization.repository.ResidentVehicleRepository;
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
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SectorizationService {
    private final BusinessUnitRepository businessUnitRepository;
    private final BusinessSectorRepository businessSectorRepository;
    private final ResidentDependentRepository residentDependentRepository;
    private final ResidentVehicleRepository residentVehicleRepository;
    private final ResidentRepository residentRepository;
    private final HrEmployeeAssignmentRepository hrEmployeeAssignmentRepository;
    private final HrEmployeeDependentRepository hrEmployeeDependentRepository;
    private final HrEmployeeRepository hrEmployeeRepository;
    private final PersonDocumentRepository personDocumentRepository;
    private final AuditRecordRepository auditRecordRepository;
    private final Path storageRoot;

    public SectorizationService(
            BusinessUnitRepository businessUnitRepository,
            BusinessSectorRepository businessSectorRepository,
            ResidentDependentRepository residentDependentRepository,
            ResidentVehicleRepository residentVehicleRepository,
            ResidentRepository residentRepository,
            HrEmployeeAssignmentRepository hrEmployeeAssignmentRepository,
            HrEmployeeDependentRepository hrEmployeeDependentRepository,
            HrEmployeeRepository hrEmployeeRepository,
            PersonDocumentRepository personDocumentRepository,
            AuditRecordRepository auditRecordRepository,
            @Value("${vmab.storage-root}") String storageRoot
    ) {
        this.businessUnitRepository = businessUnitRepository;
        this.businessSectorRepository = businessSectorRepository;
        this.residentDependentRepository = residentDependentRepository;
        this.residentVehicleRepository = residentVehicleRepository;
        this.residentRepository = residentRepository;
        this.hrEmployeeAssignmentRepository = hrEmployeeAssignmentRepository;
        this.hrEmployeeDependentRepository = hrEmployeeDependentRepository;
        this.hrEmployeeRepository = hrEmployeeRepository;
        this.personDocumentRepository = personDocumentRepository;
        this.auditRecordRepository = auditRecordRepository;
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    @PostConstruct
    void bootstrapDefaults() {
        if (businessUnitRepository.count() == 0) {
            BusinessUnit condominium = businessUnitRepository.save(new BusinessUnit("Condominio Central", BusinessUnitType.CONDOMINIUM, null, true, "Unidade padrao para demonstracao"));
            BusinessUnit neighborhood = businessUnitRepository.save(new BusinessUnit("Bairro Modelo", BusinessUnitType.NEIGHBORHOOD, null, true, "Unidade padrao para demonstracao"));
            businessSectorRepository.save(new BusinessSector(condominium.getId(), "Portaria", "PORT", true, "Setor padrao"));
            businessSectorRepository.save(new BusinessSector(condominium.getId(), "Ronda", "ROND", true, "Setor padrao"));
            businessSectorRepository.save(new BusinessSector(neighborhood.getId(), "Supervisor", "SUP", true, "Setor padrao"));
        }
    }

    @Transactional(readOnly = true)
    public List<BusinessUnit> listBusinessUnits() {
        return businessUnitRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public BusinessUnit createBusinessUnit(CreateBusinessUnitRequest request) {
        BusinessUnit unit = businessUnitRepository.save(new BusinessUnit(
                request.name().trim(),
                request.type(),
                normalizeOptional(request.cnpj()),
                request.active(),
                normalizeOptionalText(request.notes(), 1000)
        ));
        recordAudit(AuditActionType.CREATE, "BusinessUnit", unit.getId(), "Cadastro da unidade " + unit.getName());
        return unit;
    }

    @Transactional(readOnly = true)
    public List<BusinessSector> listBusinessSectors(Long businessUnitId) {
        if (!businessUnitRepository.existsById(businessUnitId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade de negocio não encontrada.");
        }
        return businessSectorRepository.findAllByBusinessUnitIdOrderByIdAsc(businessUnitId);
    }

    @Transactional
    public BusinessSector createBusinessSector(Long businessUnitId, CreateBusinessSectorRequest request) {
        BusinessUnit businessUnit = getBusinessUnit(businessUnitId);
        BusinessSector sector = businessSectorRepository.save(new BusinessSector(
                businessUnit.getId(),
                request.name().trim(),
                normalizeOptional(request.code()),
                request.active(),
                normalizeOptionalText(request.notes(), 1000)
        ));
        recordAudit(AuditActionType.CREATE, "BusinessSector", sector.getId(), "Cadastro do setor " + sector.getName() + " na unidade " + businessUnit.getName());
        return sector;
    }

    @Transactional
    public HrEmployeeAssignment assignEmployeeToSector(Long employeeId, CreateHrEmployeeAssignmentRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        BusinessUnit businessUnit = getBusinessUnit(request.businessUnitId());
        BusinessSector sector = null;
        if (request.businessSectorId() != null) {
            sector = getBusinessSector(request.businessSectorId());
            if (!sector.getBusinessUnitId().equals(businessUnit.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O setor não pertence a unidade de negocio informada.");
            }
        }
        HrEmployeeAssignment assignment = hrEmployeeAssignmentRepository.save(new HrEmployeeAssignment(
                employee.getId(),
                businessUnit.getId(),
                sector == null ? null : sector.getId(),
                request.roleTitle().trim(),
                request.startDate(),
                request.endDate(),
                request.active(),
                normalizeOptionalText(request.notes(), 1000)
        ));
        recordAudit(AuditActionType.CREATE, "HrEmployeeAssignment", assignment.getId(), "Vinculo do funcionário " + employee.getFullName() + " a unidade " + businessUnit.getName());
        return assignment;
    }

    @Transactional(readOnly = true)
    public List<HrEmployeeAssignment> listEmployeeAssignments(Long employeeId) {
        ensureEmployeeExists(employeeId);
        return hrEmployeeAssignmentRepository.findAllByEmployeeIdOrderByIdAsc(employeeId);
    }

    @Transactional
    public ResidentDependent addResidentDependent(Long residentId, CreateResidentDependentRequest request) {
        Resident resident = getResident(residentId);
        ResidentDependent dependent = residentDependentRepository.save(new ResidentDependent(
                resident.getId(),
                request.fullName().trim(),
                normalizeOptional(request.cpf()),
                normalizeOptional(request.phoneNumber()),
                request.relationship(),
                request.accessEnabled(),
                request.appEnabled(),
                normalizeOptionalText(request.notes(), 1000)
        ));
        recordAudit(AuditActionType.CREATE, "ResidentDependent", dependent.getId(), "Dependente cadastrado para o morador " + resident.getFullName());
        return dependent;
    }

    @Transactional(readOnly = true)
    public List<ResidentDependent> listResidentDependents(Long residentId) {
        ensureResidentExists(residentId);
        return residentDependentRepository.findAllByResidentIdOrderByIdAsc(residentId);
    }

    @Transactional
    public ResidentVehicle addResidentVehicle(Long residentId, CreateResidentVehicleRequest request) {
        Resident resident = getResident(residentId);
        ResidentVehicle vehicle = residentVehicleRepository.save(new ResidentVehicle(
                resident.getId(),
                request.plate().trim().toUpperCase(),
                normalizeOptional(request.model()),
                normalizeOptional(request.color()),
                request.active(),
                normalizeOptionalText(request.notes(), 1000)
        ));
        recordAudit(AuditActionType.CREATE, "ResidentVehicle", vehicle.getId(), "Veiculo vinculado ao morador " + resident.getFullName());
        return vehicle;
    }

    @Transactional(readOnly = true)
    public List<ResidentVehicle> listResidentVehicles(Long residentId) {
        ensureResidentExists(residentId);
        return residentVehicleRepository.findAllByResidentIdOrderByIdAsc(residentId);
    }

    @Transactional
    public HrEmployeeDependent addEmployeeDependent(Long employeeId, CreateHrEmployeeDependentRequest request) {
        HrEmployee employee = getEmployee(employeeId);
        HrEmployeeDependent dependent = hrEmployeeDependentRepository.save(new HrEmployeeDependent(
                employee.getId(),
                request.fullName().trim(),
                normalizeOptional(request.cpf()),
                normalizeOptional(request.phoneNumber()),
                request.relationship(),
                normalizeOptionalText(request.notes(), 1000)
        ));
        recordAudit(AuditActionType.CREATE, "HrEmployeeDependent", dependent.getId(), "Dependente cadastrado para o funcionário " + employee.getFullName());
        return dependent;
    }

    @Transactional(readOnly = true)
    public List<HrEmployeeDependent> listEmployeeDependents(Long employeeId) {
        ensureEmployeeExists(employeeId);
        return hrEmployeeDependentRepository.findAllByEmployeeIdOrderByIdAsc(employeeId);
    }

    @Transactional
    public PersonDocument uploadDocument(PersonDocumentOwnerType ownerType, Long ownerId, PersonDocumentType documentType, MultipartFile file, String notes) {
        ensureOwnerExists(ownerType, ownerId);
        validateDocumentFile(file);
        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "-" + sanitizedFilename;
        Path ownerDirectory = ownerDirectory(ownerType, ownerId);
        Path destination = ownerDirectory.resolve(storedFilename);

        try {
            Files.createDirectories(ownerDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possivel armazenar o documento.");
        }

        PersonDocument document = personDocumentRepository.save(new PersonDocument(
                ownerType,
                ownerId,
                documentType,
                sanitizedFilename,
                storedFilename,
                StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream",
                file.getSize(),
                normalizeOptionalText(notes, 1000),
                resolveCurrentActorUsername(),
                OffsetDateTime.now(ZoneOffset.UTC)
        ));
        recordAudit(AuditActionType.UPDATE, "PersonDocument", document.getId(), "Envio de documento " + documentType + " para " + ownerType + " " + ownerId);
        return document;
    }

    @Transactional(readOnly = true)
    public List<PersonDocument> listDocuments(PersonDocumentOwnerType ownerType, Long ownerId) {
        ensureOwnerExists(ownerType, ownerId);
        return personDocumentRepository.findAllByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(ownerType, ownerId);
    }

    @Transactional(readOnly = true)
    public DocumentDownload downloadDocument(PersonDocumentOwnerType ownerType, Long ownerId, Long documentId) {
        PersonDocument document = personDocumentRepository.findByIdAndOwnerTypeAndOwnerId(documentId, ownerType, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado."));

        Path resourcePath = ownerDirectory(ownerType, ownerId).resolve(document.getStoredFilename());
        Resource resource = new FileSystemResource(resourcePath);
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo do documento não encontrado.");
        }

        return new DocumentDownload(resource, document.getOriginalFilename(), StringUtils.hasText(document.getContentType()) ? document.getContentType() : "application/octet-stream");
    }

    private BusinessUnit getBusinessUnit(Long id) {
        return businessUnitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade de negocio não encontrada."));
    }

    private BusinessSector getBusinessSector(Long id) {
        return businessSectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Setor não encontrado."));
    }

    private Resident getResident(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador não encontrado."));
    }

    private HrEmployee getEmployee(Long id) {
        return hrEmployeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario não encontrado."));
    }

    private void ensureResidentExists(Long residentId) {
        if (!residentRepository.existsById(residentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Morador não encontrado.");
        }
    }

    private void ensureEmployeeExists(Long employeeId) {
        if (!hrEmployeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario não encontrado.");
        }
    }

    private void ensureOwnerExists(PersonDocumentOwnerType ownerType, Long ownerId) {
        switch (ownerType) {
            case RESIDENT -> ensureResidentExists(ownerId);
            case EMPLOYEE -> ensureEmployeeExists(ownerId);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tipo de proprietario ainda não esta disponível para documentos.");
        }
    }

    private Path ownerDirectory(PersonDocumentOwnerType ownerType, Long ownerId) {
        return storageRoot.resolve("organization").resolve(ownerType.name().toLowerCase()).resolve(String.valueOf(ownerId));
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um arquivo.");
        }
        if (file.getSize() > 15L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo excede o limite de 15 MB.");
        }
    }

    private String sanitizeFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "documento.pdf";
        }
        String filename = originalFilename.trim().replaceAll("[\\\\/]+", "_");
        filename = filename.replaceAll("[^A-Za-z0-9._-]", "_");
        return filename.isBlank() ? "documento.pdf" : filename;
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private void recordAudit(AuditActionType actionType, String entityName, Long entityId, String description) {
        AuditRecord record = new AuditRecord(
                actionType,
                entityName,
                entityId,
                resolveCurrentActorUsername(),
                OffsetDateTime.now(ZoneOffset.UTC),
                description
        );
        auditRecordRepository.save(record);
    }

    private String resolveCurrentActorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName()) || "anonymousUser".equals(authentication.getName())) {
            return "sistema";
        }
        return authentication.getName();
    }

    public record DocumentDownload(Resource resource, String originalFilename, String contentType) {
    }
}


