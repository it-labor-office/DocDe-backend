package com.docde.domain.checkin.repository;

import com.docde.domain.checkin.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    @Query("SELECT c.patient.id FROM CheckIn c")
    List<Long> findPatientId();

    Optional<CheckIn> findByPatientId(Long patientId);

    @Query("SELECT c FROM CheckIn c WHERE c.doctor.hospital.id = :hospitalId")
    List<CheckIn> findAllByHospitalId(Long hospitalId);
}
