package com.docde.domain.reservation.contorller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.common.enums.UserRole;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.reservation.dto.ReservationDoctorRequest;
import com.docde.domain.reservation.dto.ReservationDoctorResponse;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.service.ReservationDoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class ReservationDoctorController {
    private final ReservationDoctorService reservationDoctorService;

    @PutMapping("/reservations/{reservationId}/approval")
    @Secured({UserRole.Authority.DOCTOR, UserRole.Authority.DOCTOR_PRESIDENT})
    public ApiResponse<ReservationDoctorResponse.ReservationWithDoctor> approvalReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthUser authUser) {
        Reservation reservation = reservationDoctorService.approvalReservation(reservationId, authUser);
        return ApiResponse.onCreated(new ReservationDoctorResponse.ReservationWithDoctor(reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), new DoctorResponse(reservation.getDoctor())));
    }

    @PutMapping("/reservations/{reservationId}/refusal")
    @Secured({UserRole.Authority.DOCTOR, UserRole.Authority.DOCTOR_PRESIDENT})
    public ApiResponse<ReservationDoctorResponse.ReservationWithDoctor> refusalReservation(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReservationDoctorRequest.RejectReservation rejectReservationRequestDto,
            @AuthenticationPrincipal AuthUser authUser) {
        Reservation reservation = reservationDoctorService.refusalReservation(reservationId, rejectReservationRequestDto.rejectionReason(), authUser);
        return ApiResponse.onCreated(new ReservationDoctorResponse.ReservationWithDoctor(reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), new DoctorResponse(reservation.getDoctor())));
    }

    @PutMapping("/reservations/{reservationId}/done")
    public ApiResponse<ReservationDoctorResponse.ReservationWithDoctor> doneReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthUser authUser) {
        Reservation reservation = reservationDoctorService.doneReservation(reservationId, authUser);
        return ApiResponse.onCreated(new ReservationDoctorResponse.ReservationWithDoctor(reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), new DoctorResponse(reservation.getDoctor())));
    }
}
