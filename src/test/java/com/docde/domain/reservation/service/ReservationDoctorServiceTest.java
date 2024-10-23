package com.docde.domain.reservation.service;

import com.docde.common.Apiresponse.ErrorStatus;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationDoctorServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ReservationDoctorService reservationDoctorService;

    private Long doctorId = 1L;
    private Long patientId = 1L;
    private Long reservationId = 1L;

    @Nested
    class ApprovalReservationTest{

        @Test
        void 예약_승인_의사_없음_오류_테스트(){

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.approvalReservation(doctorId, patientId, reservationId)
            );

            assertEquals("의사를 찾을 수 없습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_승인_환자_없음_오류_테스트(){
            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.approvalReservation(doctorId, patientId, reservationId)
              );

            assertEquals("환자를 찾을 수 없습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }


        @Test
        void 예약_승인_예약_없음_오류_테스트(){
            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.empty());

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.approvalReservation(doctorId, patientId, reservationId)
            );

            assertEquals("예약을 찾을 수 없습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }


        @Test
        void 예약_승인_상태_예약_오류_테스트(){

            Reservation reservation = new Reservation();
            reservation.setReservationStatus(ReservationStatus.RESERVED);

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.of(reservation));

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.approvalReservation(doctorId, patientId, reservationId)
            );

            assertEquals("이미 예약이 승인 되었습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_승인_상태_진료완료_오류_테스트(){
            Reservation reservation = new Reservation();
            reservation.setReservationStatus(ReservationStatus.DONE);

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.of(reservation));

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.approvalReservation(doctorId, patientId, reservationId)
            );

            assertEquals("이미 진료가 끝난 예약입니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_승인_상태_취소_오류_테스트(){
            Reservation reservation = new Reservation();
            reservation.setReservationStatus(ReservationStatus.RESERVATION_CANCELED);

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.of(reservation));

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.approvalReservation(doctorId, patientId, reservationId)
            );

            assertEquals("이미 취소된 예약입니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_승인_정상_테스트(){
            Reservation reservation = new Reservation();

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.of(reservation));

            reservation.setReservationStatus(ReservationStatus.WAITING_RESERVATION);

            ReservationResponseDto reservationResponseDto = reservationDoctorService.approvalReservation(doctorId,patientId,reservationId);

            assertNotNull(reservationResponseDto);
            assertEquals(ReservationStatus.RESERVED, reservationResponseDto.getReservationStatus());
        }
    }

    @Nested
    class RefusalReservationTest{
        @Test
        void 예약_거부_사유_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.refusalReservation(doctorId,patientId,reservationId, reservationRequestDto)
            );

            assertEquals("거부 사유가 없으면 안됩니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_거부_의사_없음_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setRejectionReason("거부 사유");

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.refusalReservation(doctorId, patientId, reservationId,reservationRequestDto)
            );

            assertEquals("의사를 찾을 수 없습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_거부_환자_없음_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setRejectionReason("거부 사유");

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.refusalReservation(doctorId, patientId, reservationId,reservationRequestDto)
            );

            assertEquals("환자를 찾을 수 없습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }


        @Test
        void 예약_거부_예약_없음_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setRejectionReason("거부 사유");

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.empty());

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.refusalReservation(doctorId, patientId, reservationId,reservationRequestDto)
            );

            assertEquals("예약을 찾을 수 없습니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_거부_상태_거부_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setRejectionReason("거부 사유");

            Reservation reservation = new Reservation();
            reservation.setReservationStatus(ReservationStatus.RESERVATION_DENIED);

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.of(reservation));

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.refusalReservation(doctorId, patientId, reservationId,reservationRequestDto)
            );

            assertEquals("진료가 거부된 예약입니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_거부_상태_취소_오류_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setRejectionReason("거부 사유");

            Reservation reservation = new Reservation();
            reservation.setReservationStatus(ReservationStatus.RESERVATION_CANCELED);

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.of(reservation));

            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationDoctorService.refusalReservation(doctorId, patientId, reservationId,reservationRequestDto)
            );

            assertEquals("이미 취소된 예약입니다.", apiException.getErrorCode().getReasonHttpStatus().getMessage());
        }

        @Test
        void 예약_거부_정상_테스트(){
            ReservationRequestDto reservationRequestDto = new ReservationRequestDto();
            reservationRequestDto.setRejectionReason("거부 사유");

            Reservation reservation = new Reservation();
            reservation.setReservationStatus(ReservationStatus.WAITING_RESERVATION);

            when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(new Doctor()));
            when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
            when(reservationRepository.findByIdAndDoctorAndPatient(anyLong(),any(Doctor.class),any(Patient.class))).thenReturn(Optional.of(reservation));

            ReservationResponseDto reservationResponseDto = reservationDoctorService.refusalReservation(doctorId, patientId, reservationId, reservationRequestDto);

            assertNotNull(reservationResponseDto);
            assertEquals(ReservationStatus.RESERVATION_DENIED, reservationResponseDto.getReservationStatus());
            assertEquals("거부 사유", reservationResponseDto.getRejectionReason());
        }


    }
}