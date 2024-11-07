package com.docde.domain.reservation.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.aop.Lockable;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationPatientService {
    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;



    @Lockable // 다른 쓰레드에서 접근x -> 동시성 문제 방지
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation createReservation(Long doctorId, LocalDateTime reservationTime, String reservationReason, AuthUser authUser) {

        LocalDate today = LocalDate.now();

        // 예약 날짜 유효성 검사
        if (reservationTime.isBefore(today.atStartOfDay()) || reservationTime.isAfter(today.plusDays(2).atStartOfDay())) {
            throw new ApiException(ErrorStatus._INVALID_RESERVATION_DATE);
        }

        Optional<Reservation> existingReservation = reservationRepository.findByDoctorIdAndReservationTime(doctorId, reservationTime);
        if (existingReservation.isPresent()) {
            throw new ApiException(ErrorStatus._DUPLICATE_RESERVATION);
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        Patient patient = patientRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));

        // 새로운 예약 객체 생성
        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.WAITING_RESERVATION)
                .reservationTime(reservationTime)
                .reservationReason(reservationReason)
                .doctor(doctor)
                .patient(patient)
                .build();

        // 예약 저장
        Reservation savedReservation = reservationRepository.save(reservation);
        System.out.println("예약요청된 ID " + savedReservation.getId());

        return savedReservation;
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
