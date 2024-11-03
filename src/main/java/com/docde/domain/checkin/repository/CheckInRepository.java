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

    // 넘버가 가장 큰 것을 찾고 널이면 0을 반환
    @Query("SELECT COALESCE(MAX (c.number), 0) FROM CheckIn c")
    Long maxNum();

    @Query("UPDATE CheckIn c SET c.number = 0")
    void resetNum();

    // 특정 병원의 대기 중인 접수 모두 찾기
    @Query("SELECT c FROM CheckIn c WHERE c.doctor.hospital.id = :hospitalId AND c.checkinStatus = :WATING")
    List<CheckIn> findAllWaitingByHospitalId(Long hospitalId);
}
