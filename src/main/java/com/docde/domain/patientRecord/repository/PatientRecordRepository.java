package com.docde.domain.patientRecord.repository;

import com.docde.domain.patientRecord.entity.PatientRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRecordRepository extends JpaRepository<PatientRecord, Long> {
}
