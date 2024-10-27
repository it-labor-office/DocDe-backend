package com.docde.domain.reservation.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationDoctorServiceTest {
    @InjectMocks
    ReservationDoctorService reservationDoctorService;

    @Mock
    ReservationRepository reservationRepository;

    @Nested
    @DisplayName("ReservationDoctorService::approvalReservation")
    class Test1 {
        @Test
        @DisplayName("예약을 찾지 못하면 예외가 발생한다.")
        void test1() {
            // given
            Long reservationId = 1L;
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.empty());
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.approvalReservation(reservationId, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_FOUND_RESERVATION);
        }

        @Test
        @DisplayName("로그인 유저의 병원과 예약 병원이 다르면 예외가 발생한다.")
        void test2() {
            // given
            Long reservationId = 1L;
            Long hospitalId = 1L;
            Long authUserHospitalId = 2L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.approvalReservation(reservationId, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        @Test
        @DisplayName("이미 예약 되었으면 예외가 발생한다.")
        void test3() {
            // given
            Long reservationId = 1L;
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.RESERVED).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.approvalReservation(reservationId, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._ALREADY_RESERVED_RESERVATION);
        }

        @Test
        @DisplayName("이미 완료 되었으면 예외가 발생한다.")
        void test4() {
            // given
            Long reservationId = 1L;
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.DONE).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.approvalReservation(reservationId, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._ALREADY_DONE_RESERVATION);
        }

        @Test
        @DisplayName("이미 취소 되었으면 예외가 발생한다.")
        void test5() {
            // given
            Long reservationId = 1L;
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.RESERVATION_CANCELED).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.approvalReservation(reservationId, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._ALREADY_CANCEL_RESERVATION);
        }

        @Test
        @DisplayName("정상적으로 상태가 변경된다.")
        void test6() {
            // given
            Long reservationId = 1L;
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.WAITING_RESERVATION).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();
            when(reservationRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

            // when & then
            Reservation reservation1 = assertDoesNotThrow(() -> reservationDoctorService.approvalReservation(reservationId, authUser));
            assertEquals(reservation1.getStatus(), ReservationStatus.RESERVED);
        }
    }

    @Nested
    @DisplayName("ReservationDoctorService::refusalReservation")
    class Test2 {
        @Test
        @DisplayName("예약을 찾지 못하면 예외가 발생한다.")
        void test1() {
            // given
            Long reservationId = 1L;
            String rejectionReason = "reason";
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.empty());
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.refusalReservation(reservationId, rejectionReason, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_FOUND_RESERVATION);
        }

        @Test
        @DisplayName("로그인 유저의 병원과 예약 병원이 다르면 예외가 발생한다.")
        void test2() {
            // given
            Long reservationId = 1L;
            String rejectionReason = "reason";
            Long hospitalId = 1L;
            Long authUserHospitalId = 2L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.refusalReservation(reservationId, rejectionReason, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        @Test
        @DisplayName("이미 거부 되었으면 예외가 발생한다.")
        void test3() {
            // given
            Long reservationId = 1L;
            String rejectionReason = "reason";
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.RESERVATION_DENIED).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.refusalReservation(reservationId, rejectionReason, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._DENIED_RESERVATION);
        }

        @Test
        @DisplayName("이미 취소 되었으면 예외가 발생한다.")
        void test4() {
            // given
            Long reservationId = 1L;
            String rejectionReason = "reason";
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.RESERVATION_CANCELED).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.refusalReservation(reservationId, rejectionReason, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._ALREADY_CANCEL_RESERVATION);
        }

        @Test
        @DisplayName("정상적으로 상태가 변경된다.")
        void test6() {
            // given
            Long reservationId = 1L;
            String rejectionReason = "reason";
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.WAITING_RESERVATION).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();
            when(reservationRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

            // when & then
            Reservation reservation1 = assertDoesNotThrow(() -> reservationDoctorService.refusalReservation(reservationId, rejectionReason, authUser));
            assertEquals(reservation1.getStatus(), ReservationStatus.RESERVATION_DENIED);
        }
    }

    @Nested
    @DisplayName("ReservationDoctorService::doneReservation")
    class Test3 {
        @Test
        @DisplayName("예약을 찾지 못하면 예외가 발생한다.")
        void test1() {
            // given
            Long reservationId = 1L;
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.empty());
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.doneReservation(reservationId, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_FOUND_RESERVATION);
        }

        @Test
        @DisplayName("로그인 유저의 병원과 예약 병원이 다르면 예외가 발생한다.")
        void test2() {
            // given
            Long reservationId = 1L;
            Long hospitalId = 1L;
            Long authUserHospitalId = 2L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.doneReservation(reservationId, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        @Test
        @DisplayName("이미 예약 상태 아니면 예외가 발생한다.")
        void test3() {
            // given
            Long reservationId = 1L;
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.RESERVATION_DENIED).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationDoctorService.doneReservation(reservationId, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_RESERVED_RESERVATION);
        }

        @Test
        @DisplayName("정상적으로 상태가 변경된다.")
        void test6() {
            // given
            Long reservationId = 1L;
            Long hospitalId = 1L;
            Long authUserHospitalId = 1L;
            Hospital hospital = Hospital.builder().build();
            Doctor doctor = Doctor.builder().hospital(hospital).build();
            Reservation reservation = Reservation.builder().doctor(doctor).status(ReservationStatus.RESERVED).build();
            ReflectionTestUtils.setField(hospital, "id", hospitalId);
            when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();
            when(reservationRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

            // when & then
            Reservation reservation1 = assertDoesNotThrow(() -> reservationDoctorService.doneReservation(reservationId, authUser));
            assertEquals(reservation1.getStatus(), ReservationStatus.DONE);
        }
    }
}
