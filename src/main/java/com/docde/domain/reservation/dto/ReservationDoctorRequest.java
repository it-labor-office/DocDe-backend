package com.docde.domain.reservation.dto;

import com.docde.domain.reservation.dto.ReservationDoctorRequest.RejectReservation;
import jakarta.validation.constraints.NotBlank;

public sealed interface ReservationDoctorRequest permits RejectReservation {
    record RejectReservation(@NotBlank String rejectionReason) implements ReservationDoctorRequest {
    }
}
