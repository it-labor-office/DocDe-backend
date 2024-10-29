package com.docde.domain.medicalRecord.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DoctorMedicalRecordRequestDto {

    private Long doctorId;              // 의사 ID
    private Long patientId;             // 환자 ID
    private String description;         // 진료 내용
    private LocalDateTime consultation; // 진료 일시
    private String treatmentPlan;       // 치료 계획
    private String doctorComment;       // 의사 코멘트

    public DoctorMedicalRecordRequestDto(Long doctorId, Long patientId, String description,
                                         LocalDateTime consultation, String treatmentPlan, String doctorComment) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.description = description;
        this.consultation = consultation;
        this.treatmentPlan = treatmentPlan;
        this.doctorComment = doctorComment;
    }
}
