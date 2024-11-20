/*
package com.docde.domain.reservation.service;

import com.docde.common.response.ErrorStatus;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationPatientServiceTest {
    @InjectMocks
    ReservationPatientService reservationPatientService;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    DoctorRepository doctorRepository;

    @Mock
    PatientRepository patientRepository;

    @Nested
    @DisplayName("ReservationPatientService::createReservation")
    class Test1 {

        LocalDateTime reservationTime = LocalDateTime.now();

        @Test
        @DisplayName("의사를 찾지 못하면 예외 발생")
        void test1() {
            // given
            Long doctorId = 1L;
            String reservationReason = "reason";
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.createReservation(doctorId, reservationTime, reservationReason, authUser));
            assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_FOUND_DOCTOR);
        }

        @Test
        @DisplayName("환자를 찾지 못하면 예외 발생")
        void test2() {
            Long doctorId = 1L;
            Long userId = 2L;
            Doctor doctor = Doctor.builder().build();
            String reservationReason = "reason";
            LocalDateTime reservationTime = LocalDateTime.now().plusDays(1);

            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(patientRepository.findByUser_Id(userId)).thenReturn(Optional.empty());
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).id(userId).build();

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () ->
                    reservationPatientService.createReservation(doctorId, reservationTime, reservationReason, authUser)
            );
            assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_FOUND_PATIENT);
        }

        @Test
        @DisplayName("정상적으로 예약 된다.")
        void test3() {
            // given
            Long doctorId = 1L;
            Long userId = 2L;
            Doctor doctor = Doctor.builder().build();
            Patient patient = Patient.builder().build();
            String reservationReason = "reason";
            LocalDateTime reservationTime = LocalDateTime.now().plusDays(1);

            ReflectionTestUtils.setField(doctor, "id", doctorId);
            ReflectionTestUtils.setField(patient, "id", userId);

            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(patientRepository.findByUser_Id(userId)).thenReturn(Optional.of(patient));
            AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).id(userId).build();

            when(reservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Reservation reservation = assertDoesNotThrow(() ->
                    reservationPatientService.createReservation(doctorId, reservationTime, reservationReason, authUser)
            );

            // then
            assertEquals(reservation.getStatus(), ReservationStatus.WAITING_RESERVATION);
            assertEquals(reservation.getDoctor().getId(), doctorId);
            assertEquals(reservation.getPatient().getId(), userId);
            assertEquals(reservation.getReservationReason(), reservationReason);
            assertNull(reservation.getRejectReason());
        }

        @Nested
        @DisplayName("ReservationPatientService::cancelReservation")
        class Test2 {
            @Test
            @DisplayName("예약을 찾을 수 없으면 예외가 발생한다.")
            void test1() {
                // given
                Long reservationId = 1L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).build();
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.empty());

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.cancelReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_FOUND_RESERVATION);
            }

            @Test
            @DisplayName("로그인한 유저와 예약 유저가 다르면 예외가 발생한다.")
            void test2() {
                // given
                Long reservationId = 1L;
                Long authUserPatientId = 1L;
                Long patientId = 2L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).patientId(authUserPatientId).build();
                Patient patient = Patient.builder().build();
                ReflectionTestUtils.setField(patient, "id", patientId);
                Reservation reservation = Reservation.builder().patient(patient).build();
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.cancelReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN);
            }

            @Test
            @DisplayName("이미 취소되었으면 예외가 발생한다.")
            void test3() {
                // given
                Long reservationId = 1L;
                Long authUserPatientId = 1L;
                Long patientId = 1L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).patientId(authUserPatientId).build();
                Patient patient = Patient.builder().build();
                ReflectionTestUtils.setField(patient, "id", patientId);
                Reservation reservation = Reservation.builder().patient(patient).status(ReservationStatus.RESERVATION_CANCELED).build();
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.cancelReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._ALREADY_CANCEL_RESERVATION);
            }

            @Test
            @DisplayName("이미 진료되었으면 예외가 발생한다.")
            void test4() {
                // given
                Long reservationId = 1L;
                Long authUserPatientId = 1L;
                Long patientId = 1L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).patientId(authUserPatientId).build();
                Patient patient = Patient.builder().build();
                ReflectionTestUtils.setField(patient, "id", patientId);
                Reservation reservation = Reservation.builder().patient(patient).status(ReservationStatus.DONE).build();
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.cancelReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._ALREADY_DONE_RESERVATION);
            }

            @Test
            @DisplayName("이미 거부었으면 예외가 발생한다.")
            void test5() {
                // given
                Long reservationId = 1L;
                Long authUserPatientId = 1L;
                Long patientId = 1L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).patientId(authUserPatientId).build();
                Patient patient = Patient.builder().build();
                ReflectionTestUtils.setField(patient, "id", patientId);
                Reservation reservation = Reservation.builder().patient(patient).status(ReservationStatus.RESERVATION_DENIED).build();
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.cancelReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._DENIED_RESERVATION);
            }

            @Test
            @DisplayName("상태가 변경된다.")
            void test6() {
                // given
                Long reservationId = 1L;
                Long authUserPatientId = 1L;
                Long patientId = 1L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).patientId(authUserPatientId).build();
                Patient patient = Patient.builder().build();
                ReflectionTestUtils.setField(patient, "id", patientId);
                Reservation reservation = Reservation.builder().patient(patient).status(ReservationStatus.WAITING_RESERVATION).build();
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));
                when(reservationRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

                // when
                Reservation reservation1 = assertDoesNotThrow(() -> reservationPatientService.cancelReservation(reservationId, authUser));

                // then
                assertEquals(reservation1.getStatus(), ReservationStatus.RESERVATION_CANCELED);
            }
        }

        @Nested
        @DisplayName("ReservationPatientService::getReservation")
        class Test3 {
            @Test
            @DisplayName("예약을 찾을 수 없으면 예외가 발생한다.")
            void test1() {
                // given
                Long reservationId = 1L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).build();
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.empty());

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.getReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_FOUND_RESERVATION);
            }

            @Test
            @DisplayName("의사 병원장일때 해당 병원에 속하지 않으면 예외 발생")
            void test2() {
                // given
                Long reservationId = 1L;
                Long authUserHospitalId = 1L;
                Long hospitalId = 2L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).hospitalId(authUserHospitalId).build();
                Hospital hospital = Hospital.builder().build();
                Doctor doctor = Doctor.builder().hospital(hospital).build();
                Reservation reservation = Reservation.builder().doctor(doctor).build();
                ReflectionTestUtils.setField(reservation, "id", reservationId);
                ReflectionTestUtils.setField(hospital, "id", hospitalId);
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.getReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
            }

            @Test
            @DisplayName("의사 병원장일때 해당 병원에 속하지 않으면 예외 발생")
            void test3() {
                // given
                Long reservationId = 1L;
                Long hospitalId = 2L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_DOCTOR).build();
                Hospital hospital = Hospital.builder().build();
                Doctor doctor = Doctor.builder().hospital(hospital).build();
                Reservation reservation = Reservation.builder().doctor(doctor).build();
                ReflectionTestUtils.setField(reservation, "id", reservationId);
                ReflectionTestUtils.setField(hospital, "id", hospitalId);
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.getReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
            }

            @Test
            @DisplayName("환자일때 예약한 환자 속하지 않으면 예외 발생")
            void test4() {
                // given
                Long reservationId = 1L;
                Long hospitalId = 2L;
                Long patientId = 1L;
                Long authUserPatientId = 2L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).patientId(authUserPatientId).build();
                Hospital hospital = Hospital.builder().build();
                Patient patient = Patient.builder().build();
                Reservation reservation = Reservation.builder().patient(patient).build();
                ReflectionTestUtils.setField(reservation, "id", reservationId);
                ReflectionTestUtils.setField(hospital, "id", hospitalId);
                ReflectionTestUtils.setField(patient, "id", patientId);
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));

                // when & then
                ApiException apiException = assertThrows(ApiException.class, () -> reservationPatientService.getReservation(reservationId, authUser));
                assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN);
            }

            @Test
            @DisplayName("정상적으로 불러와진다.")
            void test5() {
                // given
                Long reservationId = 1L;
                Long hospitalId = 2L;
                Long patientId = 1L;
                Long authUserPatientId = 1L;
                AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).patientId(authUserPatientId).build();
                Hospital hospital = Hospital.builder().build();
                Patient patient = Patient.builder().build();
                Reservation reservation = Reservation.builder().patient(patient).build();
                ReflectionTestUtils.setField(reservation, "id", reservationId);
                ReflectionTestUtils.setField(hospital, "id", hospitalId);
                ReflectionTestUtils.setField(patient, "id", patientId);
                when(reservationRepository.findByIdWithDoctorAndHospitalAndPatient(reservationId)).thenReturn(Optional.of(reservation));

                // when & then
                Reservation reservation1 = assertDoesNotThrow(() -> reservationPatientService.getReservation(reservationId, authUser));
                assertEquals(reservation1.getId(), reservationId);
            }
        }
    }
}
*/
