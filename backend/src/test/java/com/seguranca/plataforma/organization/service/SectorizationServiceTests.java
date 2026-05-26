package com.seguranca.plataforma.organization.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seguranca.plataforma.hr.model.HrEmployee;
import com.seguranca.plataforma.hr.model.HrEmployeeCategory;
import com.seguranca.plataforma.hr.model.HrEmployeeStatus;
import com.seguranca.plataforma.hr.repository.HrEmployeeRepository;
import com.seguranca.plataforma.operations.model.AuditRecord;
import com.seguranca.plataforma.operations.model.Resident;
import com.seguranca.plataforma.operations.model.ResidentStatus;
import com.seguranca.plataforma.operations.repository.AuditRecordRepository;
import com.seguranca.plataforma.operations.repository.ResidentRepository;
import com.seguranca.plataforma.organization.dto.CreateBusinessUnitRequest;
import com.seguranca.plataforma.organization.dto.CreateHrEmployeeAssignmentRequest;
import com.seguranca.plataforma.organization.dto.CreateResidentDependentRequest;
import com.seguranca.plataforma.organization.model.BusinessSector;
import com.seguranca.plataforma.organization.model.BusinessUnit;
import com.seguranca.plataforma.organization.model.BusinessUnitType;
import com.seguranca.plataforma.organization.model.FamilyRelationshipType;
import com.seguranca.plataforma.organization.model.HrEmployeeAssignment;
import com.seguranca.plataforma.organization.model.ResidentDependent;
import com.seguranca.plataforma.organization.repository.BusinessSectorRepository;
import com.seguranca.plataforma.organization.repository.BusinessUnitRepository;
import com.seguranca.plataforma.organization.repository.HrEmployeeAssignmentRepository;
import com.seguranca.plataforma.organization.repository.HrEmployeeDependentRepository;
import com.seguranca.plataforma.organization.repository.PersonDocumentRepository;
import com.seguranca.plataforma.organization.repository.ResidentDependentRepository;
import com.seguranca.plataforma.organization.repository.ResidentVehicleRepository;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SectorizationServiceTests {

    @Mock private BusinessUnitRepository businessUnitRepository;
    @Mock private BusinessSectorRepository businessSectorRepository;
    @Mock private ResidentDependentRepository residentDependentRepository;
    @Mock private ResidentVehicleRepository residentVehicleRepository;
    @Mock private ResidentRepository residentRepository;
    @Mock private HrEmployeeAssignmentRepository hrEmployeeAssignmentRepository;
    @Mock private HrEmployeeDependentRepository hrEmployeeDependentRepository;
    @Mock private HrEmployeeRepository hrEmployeeRepository;
    @Mock private PersonDocumentRepository personDocumentRepository;
    @Mock private AuditRecordRepository auditRecordRepository;

    private SectorizationService sectorizationService;

    @BeforeEach
    void setUp() {
        sectorizationService = new SectorizationService(
                businessUnitRepository,
                businessSectorRepository,
                residentDependentRepository,
                residentVehicleRepository,
                residentRepository,
                hrEmployeeAssignmentRepository,
                hrEmployeeDependentRepository,
                hrEmployeeRepository,
                personDocumentRepository,
                auditRecordRepository,
                System.getProperty("java.io.tmpdir")
        );
    }

    @Test
    void deveCriarNegocioComTipoEAtivo() {
        when(businessUnitRepository.save(any(BusinessUnit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessUnit unit = sectorizationService.createBusinessUnit(new CreateBusinessUnitRequest(
                "Condominio Alpha",
                BusinessUnitType.CONDOMINIUM,
                "12.345.678/0001-90",
                true,
                "Contrato residencial"
        ));

        ArgumentCaptor<BusinessUnit> captor = ArgumentCaptor.forClass(BusinessUnit.class);
        verify(businessUnitRepository).save(captor.capture());

        assertEquals("Condominio Alpha", captor.getValue().getName());
        assertEquals(BusinessUnitType.CONDOMINIUM, captor.getValue().getType());
        assertTrue(captor.getValue().isActive());
        assertEquals("12.345.678/0001-90", captor.getValue().getCnpj());
        assertEquals(unit.getName(), captor.getValue().getName());
    }

    @Test
    void deveVincularDependenteAoMoradorMestre() {
        Resident resident = new Resident("Maria Souza", "11999999999", "Rua A, 100", "Casa 2", ResidentStatus.ACTIVE, "1234", "4321");
        ReflectionTestUtils.setField(resident, "id", 1L);
        when(residentRepository.findById(1L)).thenReturn(Optional.of(resident));
        when(residentDependentRepository.save(any(ResidentDependent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResidentDependent dependent = sectorizationService.addResidentDependent(
                1L,
                new CreateResidentDependentRequest(
                        "Ana Souza",
                        "987.654.321-00",
                        "11888888888",
                        FamilyRelationshipType.DAUGHTER,
                        true,
                        true,
                        "Usa o app para solicitacoes de escolta"
                )
        );

        ArgumentCaptor<ResidentDependent> captor = ArgumentCaptor.forClass(ResidentDependent.class);
        verify(residentDependentRepository).save(captor.capture());

        assertEquals(1L, captor.getValue().getResidentId());
        assertEquals("Ana Souza", captor.getValue().getFullName());
        assertEquals(FamilyRelationshipType.DAUGHTER, captor.getValue().getRelationship());
        assertTrue(captor.getValue().isAccessEnabled());
        assertTrue(captor.getValue().isAppEnabled());
        assertEquals(dependent.getCpf(), captor.getValue().getCpf());
    }

    @Test
    void deveVincularFuncionarioAoNegocioESetor() {
        HrEmployee employee = new HrEmployee(
                "VL-01",
                "Joao Silva",
                HrEmployeeCategory.VIGILANTE,
                HrEmployeeStatus.ACTIVE,
                "123.456.789-00",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );
        ReflectionTestUtils.setField(employee, "id", 10L);
        when(hrEmployeeRepository.findById(10L)).thenReturn(Optional.of(employee));

        BusinessUnit unit = new BusinessUnit("Condominio Beta", BusinessUnitType.CONDOMINIUM, null, true, null);
        ReflectionTestUtils.setField(unit, "id", 3L);
        when(businessUnitRepository.findById(3L)).thenReturn(Optional.of(unit));

        BusinessSector sector = new BusinessSector(3L, "Portaria", "PORT", true, null);
        ReflectionTestUtils.setField(sector, "id", 5L);
        when(businessSectorRepository.findById(5L)).thenReturn(Optional.of(sector));
        when(hrEmployeeAssignmentRepository.save(any(HrEmployeeAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HrEmployeeAssignment assignment = sectorizationService.assignEmployeeToSector(
                10L,
                new CreateHrEmployeeAssignmentRequest(
                        3L,
                        5L,
                        "Supervisor de posto",
                        LocalDate.of(2026, 1, 10),
                        null,
                        true,
                        "Plantao noturno"
                )
        );

        ArgumentCaptor<HrEmployeeAssignment> captor = ArgumentCaptor.forClass(HrEmployeeAssignment.class);
        verify(hrEmployeeAssignmentRepository).save(captor.capture());

        assertEquals(10L, captor.getValue().getEmployeeId());
        assertEquals(3L, captor.getValue().getBusinessUnitId());
        assertEquals(5L, captor.getValue().getBusinessSectorId());
        assertTrue(captor.getValue().isActive());
        assertEquals("Supervisor de posto", assignment.getRoleTitle());
        assertFalse(captor.getValue().isActive() && captor.getValue().getEndDate() != null);
    }
}
