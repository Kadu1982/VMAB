package com.seguranca.plataforma.organization.web;

import com.seguranca.plataforma.organization.dto.CreateBusinessSectorRequest;
import com.seguranca.plataforma.organization.dto.CreateBusinessUnitRequest;
import com.seguranca.plataforma.organization.dto.CreateHrEmployeeAssignmentRequest;
import com.seguranca.plataforma.organization.dto.CreateHrEmployeeDependentRequest;
import com.seguranca.plataforma.organization.dto.CreateResidentDependentRequest;
import com.seguranca.plataforma.organization.dto.CreateResidentVehicleRequest;
import com.seguranca.plataforma.organization.model.BusinessSector;
import com.seguranca.plataforma.organization.model.BusinessUnit;
import com.seguranca.plataforma.organization.model.HrEmployeeAssignment;
import com.seguranca.plataforma.organization.model.HrEmployeeDependent;
import com.seguranca.plataforma.organization.model.PersonDocument;
import com.seguranca.plataforma.organization.model.PersonDocumentOwnerType;
import com.seguranca.plataforma.organization.model.PersonDocumentType;
import com.seguranca.plataforma.organization.model.ResidentDependent;
import com.seguranca.plataforma.organization.model.ResidentVehicle;
import com.seguranca.plataforma.organization.service.SectorizationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/organization")
public class SectorizationController {
    private final SectorizationService sectorizationService;

    public SectorizationController(SectorizationService sectorizationService) {
        this.sectorizationService = sectorizationService;
    }

    @GetMapping("/business-units")
    public List<BusinessUnit> listBusinessUnits() {
        return sectorizationService.listBusinessUnits();
    }

    @PostMapping("/business-units")
    public BusinessUnit createBusinessUnit(@Valid @RequestBody CreateBusinessUnitRequest request) {
        return sectorizationService.createBusinessUnit(request);
    }

    @GetMapping("/business-units/{businessUnitId}/sectors")
    public List<BusinessSector> listBusinessSectors(@PathVariable Long businessUnitId) {
        return sectorizationService.listBusinessSectors(businessUnitId);
    }

    @PostMapping("/business-units/{businessUnitId}/sectors")
    public BusinessSector createBusinessSector(@PathVariable Long businessUnitId, @Valid @RequestBody CreateBusinessSectorRequest request) {
        return sectorizationService.createBusinessSector(businessUnitId, request);
    }

    @GetMapping("/residents/{residentId}/dependents")
    public List<ResidentDependent> listResidentDependents(@PathVariable Long residentId) {
        return sectorizationService.listResidentDependents(residentId);
    }

    @PostMapping("/residents/{residentId}/dependents")
    public ResidentDependent addResidentDependent(@PathVariable Long residentId, @Valid @RequestBody CreateResidentDependentRequest request) {
        return sectorizationService.addResidentDependent(residentId, request);
    }

    @GetMapping("/residents/{residentId}/vehicles")
    public List<ResidentVehicle> listResidentVehicles(@PathVariable Long residentId) {
        return sectorizationService.listResidentVehicles(residentId);
    }

    @PostMapping("/residents/{residentId}/vehicles")
    public ResidentVehicle addResidentVehicle(@PathVariable Long residentId, @Valid @RequestBody CreateResidentVehicleRequest request) {
        return sectorizationService.addResidentVehicle(residentId, request);
    }

    @GetMapping("/employees/{employeeId}/assignments")
    public List<HrEmployeeAssignment> listEmployeeAssignments(@PathVariable Long employeeId) {
        return sectorizationService.listEmployeeAssignments(employeeId);
    }

    @PostMapping("/employees/{employeeId}/assignments")
    public HrEmployeeAssignment assignEmployeeToSector(@PathVariable Long employeeId, @Valid @RequestBody CreateHrEmployeeAssignmentRequest request) {
        return sectorizationService.assignEmployeeToSector(employeeId, request);
    }

    @GetMapping("/employees/{employeeId}/dependents")
    public List<HrEmployeeDependent> listEmployeeDependents(@PathVariable Long employeeId) {
        return sectorizationService.listEmployeeDependents(employeeId);
    }

    @PostMapping("/employees/{employeeId}/dependents")
    public HrEmployeeDependent addEmployeeDependent(@PathVariable Long employeeId, @Valid @RequestBody CreateHrEmployeeDependentRequest request) {
        return sectorizationService.addEmployeeDependent(employeeId, request);
    }

    @GetMapping("/documents")
    public List<PersonDocument> listDocuments(
            @RequestParam PersonDocumentOwnerType ownerType,
            @RequestParam Long ownerId
    ) {
        return sectorizationService.listDocuments(ownerType, ownerId);
    }

    @PostMapping(path = "/documents/{ownerType}/{ownerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PersonDocument uploadDocument(
            @PathVariable PersonDocumentOwnerType ownerType,
            @PathVariable Long ownerId,
            @RequestParam("documentType") PersonDocumentType documentType,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes
    ) {
        return sectorizationService.uploadDocument(ownerType, ownerId, documentType, file, notes);
    }

    @GetMapping("/documents/{ownerType}/{ownerId}/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable PersonDocumentOwnerType ownerType,
            @PathVariable Long ownerId,
            @PathVariable Long documentId
    ) {
        SectorizationService.DocumentDownload download = sectorizationService.downloadDocument(ownerType, ownerId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.originalFilename() + "\"")
                .body(download.resource());
    }
}


