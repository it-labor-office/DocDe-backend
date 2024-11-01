package com.docde.domain.reservation.dto;

import com.docde.domain.reservation.dto.ReservationPatientRequest.CreateReservation;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public sealed interface ReservationPatientRequest permits CreateReservation {
    record CreateReservation(@NotBlank String reservationReason,
                             @NotNull Long doctorId,
                             @FutureOrPresent @NotNull LocalDate reservationDate) implements ReservationPatientRequest {
    }
}
