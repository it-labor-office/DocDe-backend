package com.docde.domain.checkin.repository;

import com.docde.domain.checkin.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    @Query("SELECT c.patient.id FROM CheckIn c")
    List<Long> findPatientId();

    @Query("SELECT c FROM CheckIn c WHERE c.patient.id = :patientId AND c.checkinStatus = 0L")
    Optional<CheckIn> findByPatientId(Long patientId);

    // 특정 환자의 접수 기록 중 진행중인 것 있는지 찾기
    @Query("SELECT COUNT(c) > 0 FROM CheckIn c WHERE c.patient.id = :patientId AND c.checkinStatus = 0L")
    boolean checkCheckInExist(Long patientId);

    @Query("SELECT c FROM CheckIn c WHERE c.doctor.hospital.id = :hospitalId")
    List<CheckIn> findAllByHospitalId(Long hospitalId);

    // 특정 병원의 대기 중인 접수 모두 찾기
    @Query("SELECT c FROM CheckIn c WHERE c.doctor.hospital.id = :hospitalId AND c.checkinStatus = 0L")
    List<CheckIn> findAllWaitingByHospitalId(Long hospitalId);
}
