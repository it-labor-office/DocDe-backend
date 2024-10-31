package com.docde.domain.reservation.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReservationDoctorService {
    private final ReservationRepository reservationRepository;

    @Transactional
    public Reservation approvalReservation(Long reservationId, AuthUser authUser) {
        Reservation reservation = reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_RESERVATION));
        Hospital hospital = reservation.getDoctor().getHospital();
        if (authUser.getHospitalId() == null || !hospital.getId().equals(authUser.getHospitalId()))
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);

        ReservationStatus reservationStatus = reservation.getStatus();
        if (reservationStatus == ReservationStatus.RESERVED) {
            throw new ApiException(ErrorStatus._ALREADY_RESERVED_RESERVATION);
        } else if (reservationStatus == ReservationStatus.DONE) {
            throw new ApiException(ErrorStatus._ALREADY_DONE_RESERVATION);
        } else if (reservationStatus == ReservationStatus.RESERVATION_CANCELED) {
            throw new ApiException(ErrorStatus._ALREADY_CANCEL_RESERVATION);
        }

        reservation.setStatus(ReservationStatus.RESERVED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation refusalReservation(Long reservationId, String rejectionReason, AuthUser authUser) {
        Reservation reservation = reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_RESERVATION));
        Hospital hospital = reservation.getDoctor().getHospital();
        if (authUser.getHospitalId() == null || !hospital.getId().equals(authUser.getHospitalId()))
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);


        if (reservation.getStatus() == ReservationStatus.RESERVATION_DENIED) {
            throw new ApiException(ErrorStatus._DENIED_RESERVATION);
        } else if (reservation.getStatus() == ReservationStatus.RESERVATION_CANCELED) {
            throw new ApiException(ErrorStatus._ALREADY_CANCEL_RESERVATION);
        }

        reservation.setStatus(ReservationStatus.RESERVATION_DENIED);
        reservation.setRejectReason(rejectionReason);
        return reservationRepository.save(reservation);
    }

    public Reservation doneReservation(Long reservationId, AuthUser authUser) {
        Reservation reservation = reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_RESERVATION));
        Hospital hospital = reservation.getDoctor().getHospital();
        if (authUser.getHospitalId() == null || !hospital.getId().equals(authUser.getHospitalId()))
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);

        if (reservation.getStatus() != ReservationStatus.RESERVED)
            throw new ApiException(ErrorStatus._NOT_RESERVED_RESERVATION);
        reservation.setStatus(ReservationStatus.DONE);
        return reservationRepository.save(reservation);
    }

}
