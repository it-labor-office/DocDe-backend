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
@Transactional(readOnly = true)
@Slf4j
public class ReservationDoctorService {


    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    private final ReservationStatus WAITING_RESERVATION = ReservationStatus.WAITING_RESERVATION;
    private final ReservationStatus RESERVATION_CANCELED = ReservationStatus.RESERVATION_CANCELED;
    private final ReservationStatus DONE = ReservationStatus.DONE;
    private final ReservationStatus RESERVATION_DENIED = ReservationStatus.RESERVATION_DENIED;
    private final ReservationStatus RESERVED = ReservationStatus.RESERVED;

    @Transactional
    public ReservationResponseDto approvalReservation(Long doctorId, Long patientId, Long reservationId, UserDetails userDetails) {

        if(!checkRole(userDetails)){
            throw new ApiException(ErrorStatus._FORBIDDEN);
        }

        Doctor doctor = getDoctor(doctorId);
        Patient patient = getPatient(patientId);
        Reservation reservation = getReservationDoctorPatient(doctor, patient, reservationId);
        log.info("reservation_id ::: {}\n reservation_status ::: {}", reservation.getId(),reservation.getReservationStatus());

        if(reservation.getReservationStatus() == RESERVED){
            throw new ApiException(ErrorStatus._ALREADY_RESERVED_RESERVATION);
        }else if(reservation.getReservationStatus() == DONE){
            throw new ApiException(ErrorStatus._ALREADY_DONE_RESERVATION);
        }else if(reservation.getReservationStatus() == RESERVATION_CANCELED){
            throw new ApiException(ErrorStatus._ALREADY_CANCEL_RESERVATION);
        }

        reservation.changeReservationStatus(RESERVED);

        return ReservationResponseDto.reservationReason(reservation.getId(), reservation.getReservationStatus());
    }

    @Transactional
    public ReservationResponseDto refusalReservation(Long doctorId, Long patientId, Long reservationId, ReservationRequestDto reservationRequestDto, UserDetails userDetails) {

        if(!checkRole(userDetails)){
            throw new ApiException(ErrorStatus._FORBIDDEN);
        }

        if(reservationRequestDto.getRejectionReason() == null){
            throw new ApiException(ErrorStatus._BAD_REQUEST_RESERVATION_REJECT_REASON);
        }

        Doctor doctor = getDoctor(doctorId);
        Patient patient = getPatient(patientId);
        Reservation reservation = getReservationDoctorPatient(doctor, patient, reservationId);
        log.info("reservationStatus ::: {}", reservation.getReservationStatus());

        if(reservation.getReservationStatus() == RESERVATION_DENIED){
            throw new ApiException(ErrorStatus._DENIED_RESERVATION);
        }else if(reservation.getReservationStatus() == RESERVATION_CANCELED){
            throw new ApiException(ErrorStatus._ALREADY_CANCEL_RESERVATION);
        }

        reservation.changeReservationStatus(RESERVATION_DENIED);

        return ReservationResponseDto.rejectReservation(reservation.getId(),
                reservation.getReservationStatus(), reservationRequestDto.getRejectionReason());
    }

    public ReservationResponseDto doneReservation(Long doctorId, Long patientId, Long reservationId, UserDetails userDetails) {
        return null;
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
                authority.getAuthority().equals("ROLE_DOCTOR_PRESIDENT") || authority.getAuthority().equals("ROLE_DOCTOR"));
    }
}
