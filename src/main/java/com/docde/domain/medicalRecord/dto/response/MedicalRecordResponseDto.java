package com.docde.domain.medicalRecord.dto.response;

import lombok.Getter;

@Getter
public class MedicalRecordResponseDto {
    private final DoctorMedicalRecordResponseDto doctorRecord;
    private final PatientMedicalRecordResponseDto patientRecord;

    public MedicalRecordResponseDto(DoctorMedicalRecordResponseDto doctorRecord, PatientMedicalRecordResponseDto patientRecord) {
        this.doctorRecord = doctorRecord;
        this.patientRecord = patientRecord;
    }
}
