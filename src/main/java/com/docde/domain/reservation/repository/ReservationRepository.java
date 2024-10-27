package com.docde.domain.reservation.repository;

import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByIdAndDoctorAndPatient(Long reservationId, Doctor doctor, Patient patient);

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.doctor d LEFT JOIN FETCH d.hospital h WHERE r.id = :id")
    Optional<Reservation> findByIdWithDoctorAndHospital(@Param("id") Long id);
}
