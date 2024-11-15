package com.docde.domain.medicalRecord.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PatientMedicalRecordRequestDto {


    private String description;
    private LocalDateTime treatmentDate;
    private String treatmentPlan;

}