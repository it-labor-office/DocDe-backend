package com.docde.domain.reservation.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.reservation.dto.request.ReservationRequestDto;
import com.docde.domain.reservation.dto.response.ReservationResponseDto;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationPatientService {

    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    private final ReservationStatus WAITING_RESERVATION = ReservationStatus.WAITING_RESERVATION;
    private final ReservationStatus RESERVATION_CANCELED = ReservationStatus.RESERVATION_CANCELED;
    private final ReservationStatus DONE = ReservationStatus.DONE;

    @Transactional
    public ReservationResponseDto createReservation(Long doctorId, Long patientId, ReservationRequestDto reservationRequestDto) {

        if(reservationRequestDto.getReservationReason() == null){
            throw new ApiException(ErrorStatus._BAD_REQUEST_RESERVATION_REASON);
        }

        Doctor doctor = getDoctor(doctorId);

        Patient patient = getPatient(patientId);

        Reservation reservation = Reservation.createReservation(reservationRequestDto.getReservationReason(), WAITING_RESERVATION, doctor, patient);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponseDto.of(savedReservation.getId(), savedReservation.getReservationStatus());
    }

    @Transactional
    public ReservationResponseDto cancelReservation(Long doctorId, Long patientId, Long reservationId) {
        Doctor doctor = getDoctor(doctorId);

        Patient patient = getPatient(patientId);

        Reservation reservation = getReservationDoctorPatient(doctor, patient, reservationId);

        if(reservation.getReservationStatus() == RESERVATION_CANCELED){
            throw new ApiException(ErrorStatus._ALREADY_CANCEL_RESERVATION);
        }else if(reservation.getReservationStatus() == DONE){
            throw new ApiException(ErrorStatus._ALREADY_DONE_RESERVATION);
        }

        reservation.cancelReservation(RESERVATION_CANCELED);

        return ReservationResponseDto.of(reservation.getId(), reservation.getReservationStatus());
    }


    public ReservationResponseDto getReservation(Long doctorId, Long patientId, Long reservationId) {

        Doctor doctor = getDoctor(doctorId);

        Patient patient = getPatient(patientId);

        Reservation reservation = getReservationDoctorPatient(doctor, patient, reservationId);

        return ReservationResponseDto.of(reservation.getId(), reservation.getReservationStatus());
    }



    private Doctor getDoctor(Long doctorId) {
        return doctorRepository.findById(doctorId).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));
    }

    private Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));
    }

    private Reservation getReservationDoctorPatient(Doctor doctor, Patient patient, Long reservationId) {
        return reservationRepository.findByIdAndDoctorAndPatient(reservationId, doctor, patient).orElseThrow(() ->
                new ApiException(ErrorStatus._NOT_FOUND_RESERVATION)
        );
    }

}
