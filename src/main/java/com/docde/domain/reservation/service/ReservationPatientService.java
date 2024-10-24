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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationPatientService {

    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    private final ReservationStatus WAITING_RESERVATION = ReservationStatus.WAITING_RESERVATION;
    private final ReservationStatus RESERVATION_CANCELED = ReservationStatus.RESERVATION_CANCELED;
    private final ReservationStatus DONE = ReservationStatus.DONE;
    private final ReservationStatus RESERVATION_DENIED = ReservationStatus.RESERVATION_DENIED;

    @Transactional
    public ReservationResponseDto createReservation(Long doctorId, Long patientId, ReservationRequestDto reservationRequestDto, UserDetails userDetails) {

        if(!checkRole(userDetails)){
            throw new ApiException(ErrorStatus._FORBIDDEN);
        }

        if(reservationRequestDto.getReservationReason() == null){
            throw new ApiException(ErrorStatus._BAD_REQUEST_RESERVATION_REASON);
        }

        Doctor doctor = getDoctor(doctorId);

        Patient patient = getPatient(patientId);

        Reservation reservation = Reservation.createReservation(reservationRequestDto.getReservationReason(), WAITING_RESERVATION, doctor, patient);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponseDto.reservationReason(savedReservation.getId(),
                savedReservation.getReservationStatus(),
                reservationRequestDto.getReservationReason());
    }

    @Transactional
    public ReservationResponseDto cancelReservation(Long doctorId, Long patientId, Long reservationId, UserDetails userDetails) {

        if(!checkRole(userDetails)){
            throw new ApiException(ErrorStatus._FORBIDDEN);
        }
        Doctor doctor = getDoctor(doctorId);

        Patient patient = getPatient(patientId);

        Reservation reservation = getReservationDoctorPatient(doctor, patient, reservationId);

        if(reservation.getReservationStatus() == RESERVATION_CANCELED){
            throw new ApiException(ErrorStatus._ALREADY_CANCEL_RESERVATION);
        }else if(reservation.getReservationStatus() == DONE){
            throw new ApiException(ErrorStatus._ALREADY_DONE_RESERVATION);
        }else if(reservation.getReservationStatus() == RESERVATION_DENIED){
            throw new ApiException(ErrorStatus._DENIED_RESERVATION);
        }

        reservation.changeReservationStatus(RESERVATION_CANCELED);

        return ReservationResponseDto.reservationReason(reservation.getId(), reservation.getReservationStatus());
    }


    public ReservationResponseDto getReservation(Long doctorId, Long patientId, Long reservationId) {

        Doctor doctor = getDoctor(doctorId);

        Patient patient = getPatient(patientId);

        Reservation reservation = getReservationDoctorPatient(doctor, patient, reservationId);

        return ReservationResponseDto.reservationReason(reservation.getId(), reservation.getReservationStatus());
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
    private boolean checkRole(UserDetails userDetails){
        return userDetails.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_PATIENT"));
    }


}
