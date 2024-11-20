package com.docde.domain.reservation.dto;

import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.patient.dto.PatientResponse;
import com.docde.domain.reservation.dto.ReservationDoctorResponse.ReservationWithPatientAndDoctor;
import com.docde.domain.reservation.entity.ReservationStatus;

public sealed interface ReservationDoctorResponse permits ReservationWithPatientAndDoctor {
    record ReservationWithPatientAndDoctor(Long id, String reservationReason, ReservationStatus status,
                                           String rejectReason,
                                           PatientResponse patient,
                                           DoctorResponse doctor) implements ReservationDoctorResponse {
    }
}
