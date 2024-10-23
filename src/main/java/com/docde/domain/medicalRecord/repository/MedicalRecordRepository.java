package com.docde.domain.medicalRecord.repository;

import com.docde.domain.medicalRecord.dto.response.PatientMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /*@Query("SELECT new com.docde.domain.medicalRecord.dto.response.PatientMedicalRecordResponseDto" +
            "(m.patientRecordId, m.description, m.consultation, d.name) " +
            "FROM MedicalRecord m JOIN m.doctor d WHERE m.patient.id = :patientId")
    List<PatientMedicalRecordResponseDto> findByPatientId(Long patientId);*/

    List<MedicalRecord> findByDoctorId(Long doctorId);

    List<MedicalRecord> findByPatientId(Long patientId);
}
