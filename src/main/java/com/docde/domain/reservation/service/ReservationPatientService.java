package com.docde.domain.reservation.service;

import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.reservation.dto.response.ReservationResponseDto;
import com.docde.domain.reservation.dto.request.ReservationRequestDto;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.exception.ReservationReasonNullPointerException;
import com.docde.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationPatientService {

    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    private final ReservationStatus WAITING_RESERVATION = ReservationStatus.WAITING_RESERVATION;
    private final ReservationStatus RESERVATION_CANCELED = ReservationStatus.RESERVATION_CANCELED;

    public ReservationResponseDto createReservation(Long doctorId, Long patientId, ReservationRequestDto reservationRequestDto) {

        if(reservationRequestDto.getReservationReason() == null){
            throw new ReservationReasonNullPointerException("예약 사유는 없으면 안 됩니다.");
        }

        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() ->
                new RuntimeException("Doctor not found")
            );

        Patient patient = patientRepository.findById(patientId).orElseThrow(() ->
                new RuntimeException("Patient not found")
        );

        Reservation reservation = Reservation.createReservation(reservationRequestDto.getReservationReason(), WAITING_RESERVATION, doctor, patient);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponseDto.of(savedReservation.getId(), savedReservation.getReservationStatus());
    }

    public ReservationResponseDto cancelReservation(Long doctorId, Long patientId, Long reservationId) {
        return null;
    }


    public ReservationResponseDto getReservation(Long doctorId, Long patientId) {
        return null;
    }
}
