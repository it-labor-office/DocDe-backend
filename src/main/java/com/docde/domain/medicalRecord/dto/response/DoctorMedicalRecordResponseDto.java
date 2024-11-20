package com.docde.domain.medicalRecord.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DoctorMedicalRecordResponseDto {

    private final Long medicalRecordId;       // 진료 기록 ID
    private final String description;         // 진료 내용
    private final LocalDateTime treatmentDate; // 진료 일시
    private final String patientName;         // 환자 이름
    private final Long patientId;             // 환자 ID
    private final String treatmentPlan;       // 치료 계획
    private final String doctorComment;       // 의사 코멘트


    public DoctorMedicalRecordResponseDto(Long medicalRecordId, String description,
                                          LocalDateTime treatmentDate , String patientName,
                                          Long patientId, String treatmentPlan, String doctorComment) {
        this.medicalRecordId = medicalRecordId;
        this.description = description;
        this.treatmentDate  = treatmentDate ;
        this.patientName = patientName;
        this.patientId = patientId;
        this.treatmentPlan = treatmentPlan;
        this.doctorComment = doctorComment;
    }
}
