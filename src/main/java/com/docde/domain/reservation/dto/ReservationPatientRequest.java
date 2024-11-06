package com.docde.domain.reservation.dto;

import com.docde.domain.reservation.dto.ReservationPatientRequest.CreateReservation;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public sealed interface ReservationPatientRequest permits CreateReservation {
    record CreateReservation(@NotBlank String reservationReason,
                             @NotNull Long doctorId,
                             @FutureOrPresent @NotNull LocalDateTime reservationTime) implements ReservationPatientRequest {
    }
}
