package com.docde.domain.reservation.service;

import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.reservation.dto.request.ReservationRequestDto;
import com.docde.domain.reservation.dto.response.ReservationResponseDto;
import com.docde.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationDoctorService {


    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public ReservationResponseDto approvalReservation(Long doctorId, Long patientId, Long reservationId) {
        return null;
    }

    public ReservationResponseDto refusalReservation(Long doctorId, Long patientId, Long reservationId, ReservationRequestDto reservationRequestDto) {
        return null;
    }
}
