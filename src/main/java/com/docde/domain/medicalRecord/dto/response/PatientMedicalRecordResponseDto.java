package com.docde.domain.medicalRecord.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PatientMedicalRecordResponseDto {

    private final Long medicalRecordId;       // 진료 기록 ID
    private final String description;         // 진료 내용
    private final LocalDateTime consultation; // 진료 일시
    private final String doctorName;          // 의사 이름

    public PatientMedicalRecordResponseDto(Long medicalRecordId, String description,
                                           LocalDateTime consultation, String doctorName) {
        this.medicalRecordId = medicalRecordId;
        this.description = description;
        this.consultation = consultation;
        this.doctorName = doctorName;

    }

}

