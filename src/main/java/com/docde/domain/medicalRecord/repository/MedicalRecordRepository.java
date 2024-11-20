package com.docde.domain.medicalRecord.repository;

import com.docde.domain.medicalRecord.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long>, MedicalRecordRepositoryCustom  {

    List<MedicalRecord> findByDoctorId(Long doctorId);

    List<MedicalRecord> findByPatientId(Long patientId);


}
