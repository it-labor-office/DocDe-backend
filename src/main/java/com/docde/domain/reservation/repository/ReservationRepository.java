package com.docde.domain.reservation.repository;

import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.print.Doc;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByIdAndDoctorAndPatient (Long reservationId, Doctor doctor, Patient patient);
}
