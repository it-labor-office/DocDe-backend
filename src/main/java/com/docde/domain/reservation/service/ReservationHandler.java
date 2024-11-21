package com.docde.domain.reservation.service;

import com.docde.common.response.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.queue.DatabaseMetricsService;
import com.docde.domain.reservation.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ReservationHandler {

    private final DatabaseMetricsService databaseMetricsService;
    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private static final int RESERVATION_DAYS_ALLOWED = 2; // 예약 가능한 일수


    public Reservation handleReservation(Long doctorId, LocalDateTime reservationTime, String
            reservationReason, Long patientId) {
        try {
            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
            LocalDateTime endOfAllowedPeriod = startOfToday.plusDays(RESERVATION_DAYS_ALLOWED).withHour(23).withMinute(59).withSecond(59);

            log.info("Reservation time: {}", reservationTime);
            log.info("Start of today: {}", startOfToday);
            log.info("End of allowed period: {}", endOfAllowedPeriod);

            if (reservationTime.isBefore(startOfToday)) {
                throw new ApiException(ErrorStatus._INVALID_RESERVATION_DATE);
            }

            if (reservationTime.isAfter(endOfAllowedPeriod)) {
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

            Reservation savedReservation = reservationRepository.save(reservation);

            databaseMetricsService.recordDatabaseRequest(true); // DB 저장 성공

            return savedReservation;

        } catch (Exception e) {
            databaseMetricsService.recordDatabaseRequest(false); // DB 저장 실패
            log.error("예약 저장 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}
