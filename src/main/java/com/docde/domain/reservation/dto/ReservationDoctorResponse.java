package com.docde.domain.reservation.dto;

import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.reservation.dto.ReservationDoctorResponse.ReservationWithDoctor;
import com.docde.domain.reservation.entity.ReservationStatus;

public sealed interface ReservationDoctorResponse permits ReservationWithDoctor {
    record ReservationWithDoctor(Long id, String reservationReason, ReservationStatus status, String rejectReason,
                                 DoctorResponse doctorResponse) implements ReservationDoctorResponse {

    }
}
