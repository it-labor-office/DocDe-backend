package com.docde.domain.reservation.service;

import com.docde.common.exceptions.ApiException;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.reservation.dto.request.ReservationRequestDto;
import com.docde.domain.reservation.dto.response.ReservationResponseDto;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.print.Doc;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationPatientServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private ReservationPatientService reservationPatientService;


    private Long doctorId = 1L;
    private Long patientId = 1L;
    private Long reservationId = 1L;


    @Nested
    class CreateReservationTest {
        @Test
        void 예약_신청_사유_없음_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationPatientService.createReservation(doctorId,patientId, reservationRequestDto)
            );

            assertEquals("예약 사유가 없으면 안됩니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }


        @Test
        void 예약_신청_의사_없음_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setReservationReason("예약 신청 사유");

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationPatientService.createReservation(doctorId, patientId, reservationRequestDto)
            );

            assertEquals("의사를 찾을 수 없습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_신청_환자_없음_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setReservationReason("예약 신청 사유");

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationPatientService.createReservation(doctorId, patientId, reservationRequestDto)
            );

            assertEquals("환자를 찾을 수 없습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_신청_정상_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setReservationReason("예약 신청 사유");

            Doctor doctor = new Doctor();
            Patient patient = new Patient();

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));

            Reservation createdReservation = Reservation.createReservation(reservationRequestDto.getReservationReason(), ReservationStatus.WAITING_RESERVATION, doctor, patient);
            createdReservation.setId(1L);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(createdReservation);

            ReservationResponseDto response = reservationPatientService.createReservation(doctorId, patientId, reservationRequestDto);

            assertNotNull(response);
            assertEquals(createdReservation.getId(), response.getReservationId());
            assertEquals(createdReservation.getReservationStatus(), response.getReservationStatus());
            assertEquals(reservationRequestDto.getReservationReason(), response.getReservationReason());
            verify(reservationRepository).save(any(Reservation.class));
        }

    }
}