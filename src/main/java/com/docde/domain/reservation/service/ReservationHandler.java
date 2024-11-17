package com.docde.domain.reservation.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ReservationHandler {

    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;


    public Reservation handleReservation(Long doctorId, LocalDateTime reservationTime, String
            reservationReason, Long patientId) {

        LocalDate today = LocalDate.now();

        if (reservationTime.isBefore(today.atStartOfDay()) || reservationTime.isAfter(today.plusDays(2).atStartOfDay())) {
            throw new ApiException(ErrorStatus._INVALID_RESERVATION_DATE);
        }

        Optional<Reservation> existingReservation = reservationRepository.findByDoctorIdAndReservationTime(doctorId, reservationTime);
        if (existingReservation.isPresent()) {
            throw new ApiException(ErrorStatus._DUPLICATE_RESERVATION);
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.WAITING_RESERVATION)
                .reservationTime(reservationTime)
                .reservationReason(reservationReason)
                .doctor(doctor)
                .patient(patient)
                .build();
        System.out.println("reservation = " + reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return savedReservation;
    }
}
