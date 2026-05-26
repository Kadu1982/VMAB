package com.seguranca.plataforma.hr.dto;

import com.seguranca.plataforma.hr.model.HrEmployee;
import com.seguranca.plataforma.hr.model.HrEmployeeCategory;
import com.seguranca.plataforma.hr.model.HrEmployeeStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record HrEmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        HrEmployeeCategory category,
        HrEmployeeStatus status,
        String documentNumber,
        String address,
        String phoneNumber,
        String email,
        String photoUrl,
        String cnhCategory,
        LocalDate cnhExpiry,
        LocalDate medicalExamExpiry,
        LocalDate trainingExpiry,
        String trainingNotes,
        String documentNotes,
        LocalDate hireDate,
        LocalDate terminationDate,
        Long linkedAgentId,
        String linkedAgentName,
        Long linkedAppUserId,
        String linkedAppUserUsername,
        boolean pointEnabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime lastCheckInAt,
        OffsetDateTime lastCheckOutAt,
        String lastCheckInDevice,
        String lastCheckOutDevice,
        Double lastCheckInLatitude,
        Double lastCheckInLongitude,
        Double lastCheckOutLatitude,
        Double lastCheckOutLongitude,
        String lastPointNotes,
        boolean cnhRenewalDue,
        boolean medicalExamRenewalDue,
        boolean trainingRenewalDue
) {
    public static HrEmployeeResponse fromEntity(
            HrEmployee employee,
            String linkedAgentName,
            String linkedAppUserUsername,
            boolean cnhRenewalDue,
            boolean medicalExamRenewalDue,
            boolean trainingRenewalDue
    ) {
        return new HrEmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getCategory(),
                employee.getStatus(),
                employee.getDocumentNumber(),
                employee.getAddress(),
                employee.getPhoneNumber(),
                employee.getEmail(),
                employee.getPhotoUrl(),
                employee.getCnhCategory(),
                employee.getCnhExpiry(),
                employee.getMedicalExamExpiry(),
                employee.getTrainingExpiry(),
                employee.getTrainingNotes(),
                employee.getDocumentNotes(),
                employee.getHireDate(),
                employee.getTerminationDate(),
                employee.getLinkedAgentId(),
                linkedAgentName,
                employee.getLinkedAppUserId(),
                linkedAppUserUsername,
                employee.isPointEnabled(),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                employee.getLastCheckInAt(),
                employee.getLastCheckOutAt(),
                employee.getLastCheckInDevice(),
                employee.getLastCheckOutDevice(),
                employee.getLastCheckInLatitude(),
                employee.getLastCheckInLongitude(),
                employee.getLastCheckOutLatitude(),
                employee.getLastCheckOutLongitude(),
                employee.getLastPointNotes(),
                cnhRenewalDue,
                medicalExamRenewalDue,
                trainingRenewalDue
        );
    }
}
