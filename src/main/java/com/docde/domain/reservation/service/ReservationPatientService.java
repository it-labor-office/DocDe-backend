package com.docde.domain.reservation.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
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
@Transactional(readOnly = true)
public class ReservationPatientService {
    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public Reservation createReservation(Long doctorId, String reservationReason, AuthUser authUser) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));
        Patient patient = patientRepository.findById(authUser.getPatientId()).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));
        Reservation reservation = Reservation.builder().status(ReservationStatus.WAITING_RESERVATION).reservationReason(reservationReason).doctor(doctor).patient(patient).build();
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, AuthUser authUser) {
        Reservation reservation = reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_RESERVATION));
        if (!reservation.getPatient().getId().equals(authUser.getPatientId()))
            throw new ApiException(ErrorStatus._FORBIDDEN);

        if (reservation.getStatus() == ReservationStatus.RESERVATION_CANCELED) {
            throw new ApiException(ErrorStatus._ALREADY_CANCEL_RESERVATION);
        } else if (reservation.getStatus() == ReservationStatus.DONE) {
            throw new ApiException(ErrorStatus._ALREADY_DONE_RESERVATION);
        } else if (reservation.getStatus() == ReservationStatus.RESERVATION_DENIED) {
            throw new ApiException(ErrorStatus._DENIED_RESERVATION);
        }

        reservation.setStatus(ReservationStatus.RESERVATION_CANCELED);
        return reservationRepository.save(reservation);
    }

    public Reservation getReservation(Long reservationId, AuthUser authUser) {
        Reservation reservation = reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_RESERVATION));
        if (authUser.getUserRole().equals(UserRole.ROLE_DOCTOR) || authUser.getUserRole().equals(UserRole.ROLE_DOCTOR_PRESIDENT)) {
            if (authUser.getHospitalId() == null || !reservation.getDoctor().getHospital().getId().equals(authUser.getHospitalId()))
                throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        } else {
            if (!reservation.getPatient().getId().equals(authUser.getPatientId()))
                throw new ApiException(ErrorStatus._FORBIDDEN);
        }

        return reservation;
    }
}
