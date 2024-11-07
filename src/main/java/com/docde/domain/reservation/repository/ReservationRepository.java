package com.docde.domain.reservation.repository;

import com.docde.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.doctor d LEFT JOIN FETCH d.hospital h LEFT JOIN FETCH r.patient WHERE r.id = :id")
    Optional<Reservation> findByIdWithDoctorAndHospitalAndPatient(@Param("id") Long id);

    /*@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.doctor.id = :doctorId AND r.reservationTime = :reservationTime")
    Optional<Reservation> findByDoctorIdAndReservationTimeWithLock(@Param("doctorId") Long doctorId, @Param("reservationTime") LocalDateTime reservationTime);*/

    Optional<Reservation> findByDoctorIdAndReservationTime(Long doctorId, LocalDateTime reservationTime);
    }

