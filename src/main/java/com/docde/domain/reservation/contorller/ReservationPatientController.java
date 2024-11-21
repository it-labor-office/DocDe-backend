package com.docde.domain.reservation.contorller;


import com.docde.common.enums.UserRole;
import com.docde.common.response.ApiResponse;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.patient.dto.PatientResponse;
import com.docde.domain.reservation.dto.ReservationPatientRequest;
import com.docde.domain.reservation.dto.ReservationPatientResponse;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.service.ReservationPatientService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ReservationPatientController {
    private final ReservationPatientService reservationPatientService;

    @PostMapping("/reservations")
    @Secured(UserRole.Authority.PATIENT)
    public ResponseEntity<ApiResponse<ReservationPatientResponse.ReservationWithPatientAndDoctor>> createReservation(
            @RequestBody ReservationPatientRequest.CreateReservation createReservationRequestDto,
            @AuthenticationPrincipal AuthUser authUser) {

        // 예약 로직 실행
        Reservation reservation = reservationPatientService.createReservation(
                createReservationRequestDto.doctorId(),
                createReservationRequestDto.reservationTime(),
                createReservationRequestDto.reservationReason(),
                authUser);

        // 비동기 큐에 추가된 경우
        if (reservation == null) {
            return ApiResponse.onSuccess(
                    new ReservationPatientResponse.ReservationWithPatientAndDoctor(
                            null,
                            "예약 요청이 큐에 추가되었습니다.",
                            null,
                            null,
                            null,
                            null
                    )
            ).toEntity();
        }

        // 예약이 성공적으로 생성된 경우
        return ApiResponse.onCreated(
                new ReservationPatientResponse.ReservationWithPatientAndDoctor(
                        reservation.getId(),
                        reservation.getReservationReason(),
                        reservation.getStatus(),
                        reservation.getRejectReason(),
                        new PatientResponse(reservation.getPatient()),
                        new DoctorResponse(reservation.getDoctor())
                )
        ).toEntity();
    }

    @PutMapping("/reservations/{reservationId}/cancel")
    @Secured(UserRole.Authority.PATIENT)
    public ResponseEntity<ApiResponse<ReservationPatientResponse.ReservationWithPatientAndDoctor>> cancelReservation(@PathVariable Long reservationId, @AuthenticationPrincipal AuthUser authUser) {
        Reservation reservation = reservationPatientService.cancelReservation(reservationId, authUser);
        return ApiResponse.onSuccess(new ReservationPatientResponse.ReservationWithPatientAndDoctor(reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), new PatientResponse(reservation.getPatient()), new DoctorResponse(reservation.getDoctor()))).toEntity();
    }

    @GetMapping("/reservations/{reservationId}")
    @Secured(UserRole.Authority.PATIENT)
    public ResponseEntity<ApiResponse<ReservationPatientResponse.ReservationWithPatientAndDoctor>> getReservation(
            @PathVariable Long reservationId, @AuthenticationPrincipal AuthUser authUser) {
        Reservation reservation = reservationPatientService.getReservation(reservationId, authUser);
        return ApiResponse.onSuccess(new ReservationPatientResponse.ReservationWithPatientAndDoctor(reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), new PatientResponse(reservation.getPatient()), new DoctorResponse(reservation.getDoctor()))).toEntity();
    }
}
