package com.docde.domain.medicalRecord.repository;

import com.docde.domain.medicalRecord.entity.MedicalRecord;

import java.util.Optional;

public interface MedicalRecordRepositoryCustom {
    Optional<MedicalRecord> findSpecificMedicalRecord(Long medicalRecordId,
                                                      String description,
                                                      String treatmentPlan,
                                                      String doctorComment);
}
