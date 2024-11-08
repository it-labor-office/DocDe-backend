package com.docde.domain.reservation.contorller;


import com.docde.common.Apiresponse.ApiResponse;
import com.docde.common.enums.UserRole;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.patient.dto.PatientResponse;
import com.docde.domain.reservation.dto.ReservationPatientRequest;
import com.docde.domain.reservation.dto.ReservationPatientResponse;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.service.ReservationPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReservationPatientController {
    private final ReservationPatientService reservationPatientService;

    @PostMapping("/reservations")
    @Secured(UserRole.Authority.PATIENT)
    public ResponseEntity<ApiResponse<ReservationPatientResponse.ReservationWithPatientAndDoctor>> createReservation(
            @RequestBody ReservationPatientRequest.CreateReservation createReservationRequestDto,
            @AuthenticationPrincipal AuthUser authUser) {

        Reservation reservation = reservationPatientService.createReservation(
                createReservationRequestDto.doctorId(),
                createReservationRequestDto.reservationTime(),
                createReservationRequestDto.reservationReason(),
                authUser);

        ApiResponse<ReservationPatientResponse.ReservationWithPatientAndDoctor> response = ApiResponse.onCreated(
                new ReservationPatientResponse.ReservationWithPatientAndDoctor(
                        reservation.getId(),
                        reservation.getReservationReason(),
                        reservation.getStatus(),
                        reservation.getRejectReason(),
                        new PatientResponse(reservation.getPatient()),
                        new DoctorResponse(reservation.getDoctor())
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/reservations/{reservationId}/cancel")
    @Secured(UserRole.Authority.PATIENT)
    public ApiResponse<ReservationPatientResponse.ReservationWithPatientAndDoctor> cancelReservation(@PathVariable Long reservationId, @AuthenticationPrincipal AuthUser authUser) {
        Reservation reservation = reservationPatientService.cancelReservation(reservationId, authUser);
        return ApiResponse.onSuccess(new ReservationPatientResponse.ReservationWithPatientAndDoctor(reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), new PatientResponse(reservation.getPatient()), new DoctorResponse(reservation.getDoctor())));
    }

    @GetMapping("/reservations/{reservationId}")
    @Secured(UserRole.Authority.PATIENT)
    public ApiResponse<ReservationPatientResponse.ReservationWithPatientAndDoctor> getReservation(
            @PathVariable Long reservationId, @AuthenticationPrincipal AuthUser authUser) {
        Reservation reservation = reservationPatientService.getReservation(reservationId, authUser);
        return ApiResponse.onSuccess(new ReservationPatientResponse.ReservationWithPatientAndDoctor(reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), new PatientResponse(reservation.getPatient()), new DoctorResponse(reservation.getDoctor())));
    }
}
