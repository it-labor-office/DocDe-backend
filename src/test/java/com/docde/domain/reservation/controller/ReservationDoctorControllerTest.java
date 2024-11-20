package com.docde.domain.reservation.controller;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.config.WithMockAuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.reservation.contorller.ReservationDoctorController;
import com.docde.domain.reservation.dto.ReservationDoctorRequest;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.service.ReservationDoctorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationDoctorController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
public class ReservationDoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationDoctorService reservationDoctorService;

    @DisplayName("[PUT] /reservations/{reservationId}/approval")
    @WithMockAuthUser(userRole = UserRole.ROLE_DOCTOR)
    @Test
    void test1() throws Exception {
        // given
        Patient patient = Patient.builder().name("").address("").phone("").gender(Gender.M).build();
        Doctor doctor = Doctor.builder().name("").medicalDepartment("").build();
        Reservation reservation = Reservation
                .builder()
                .reservationReason("")
                .rejectReason("")
                .status(ReservationStatus.WAITING_RESERVATION)
                .patient(patient)
                .doctor(doctor)
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        ReflectionTestUtils.setField(patient, "id", 1L);
        ReflectionTestUtils.setField(doctor, "id", 1L);
        when(reservationDoctorService.approvalReservation(any(), any())).thenReturn(reservation);

        // when & then
        mockMvc.perform(put("/reservations/{reservationId}/approval", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(reservation.getId()))
                .andExpect(jsonPath("$.data.reservationReason").value(reservation.getReservationReason()))
                .andExpect(jsonPath("$.data.status").value(reservation.getStatus().name()))
                .andExpect(jsonPath("$.data.rejectReason").value(reservation.getRejectReason()))
                .andExpect(jsonPath("$.data.patient.id").value(reservation.getPatient().getId()))
                .andExpect(jsonPath("$.data.patient.name").value(reservation.getPatient().getName()))
                .andExpect(jsonPath("$.data.patient.address").value(reservation.getPatient().getAddress()))
                .andExpect(jsonPath("$.data.patient.phone").value(reservation.getPatient().getPhone()))
                .andExpect(jsonPath("$.data.patient.gender").value(reservation.getPatient().getGender().name()))
                .andExpect(jsonPath("$.data.doctor.name").value(doctor.getName()))
                .andExpect(jsonPath("$.data.doctor.medicalDepartment").value(doctor.getMedicalDepartment()));
    }

    @DisplayName("[PUT] /reservations/{reservationId}/refusal")
    @WithMockAuthUser(userRole = UserRole.ROLE_DOCTOR)
    @Test
    void test2() throws Exception {
        // given
        Patient patient = Patient.builder().name("").address("").phone("").gender(Gender.M).build();
        Doctor doctor = Doctor.builder().name("").medicalDepartment("").build();
        Reservation reservation = Reservation
                .builder()
                .reservationReason("")
                .rejectReason("")
                .status(ReservationStatus.WAITING_RESERVATION)
                .patient(patient)
                .doctor(doctor)
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        ReflectionTestUtils.setField(patient, "id", 1L);
        ReflectionTestUtils.setField(doctor, "id", 1L);
        ReservationDoctorRequest.RejectReservation rejectReservationRequestDto = new ReservationDoctorRequest.RejectReservation("123");
        when(reservationDoctorService.refusalReservation(any(), any(), any())).thenReturn(reservation);

        // when & then
        mockMvc.perform(put("/reservations/{reservationId}/refusal", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReservationRequestDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(reservation.getId()))
                .andExpect(jsonPath("$.data.reservationReason").value(reservation.getReservationReason()))
                .andExpect(jsonPath("$.data.status").value(reservation.getStatus().name()))
                .andExpect(jsonPath("$.data.rejectReason").value(reservation.getRejectReason()))
                .andExpect(jsonPath("$.data.patient.id").value(reservation.getPatient().getId()))
                .andExpect(jsonPath("$.data.patient.name").value(reservation.getPatient().getName()))
                .andExpect(jsonPath("$.data.patient.address").value(reservation.getPatient().getAddress()))
                .andExpect(jsonPath("$.data.patient.phone").value(reservation.getPatient().getPhone()))
                .andExpect(jsonPath("$.data.patient.gender").value(reservation.getPatient().getGender().name()))
                .andExpect(jsonPath("$.data.doctor.name").value(doctor.getName()))
                .andExpect(jsonPath("$.data.doctor.medicalDepartment").value(doctor.getMedicalDepartment()));
    }

    @DisplayName("[PUT] /reservations/{reservationId}/done")
    @WithMockAuthUser(userRole = UserRole.ROLE_DOCTOR)
    @Test
    void test3() throws Exception {
        // given
        Patient patient = Patient.builder().name("").address("").phone("").gender(Gender.M).build();
        Doctor doctor = Doctor.builder().name("").medicalDepartment("").build();
        Reservation reservation = Reservation
                .builder()
                .reservationReason("")
                .rejectReason("")
                .status(ReservationStatus.WAITING_RESERVATION)
                .patient(patient)
                .doctor(doctor)
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        ReflectionTestUtils.setField(patient, "id", 1L);
        ReflectionTestUtils.setField(doctor, "id", 1L);
        when(reservationDoctorService.doneReservation(any(), any())).thenReturn(reservation);

        // when & then
        mockMvc.perform(put("/reservations/{reservationId}/done", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(reservation.getId()))
                .andExpect(jsonPath("$.data.reservationReason").value(reservation.getReservationReason()))
                .andExpect(jsonPath("$.data.status").value(reservation.getStatus().name()))
                .andExpect(jsonPath("$.data.rejectReason").value(reservation.getRejectReason()))
                .andExpect(jsonPath("$.data.patient.id").value(reservation.getPatient().getId()))
                .andExpect(jsonPath("$.data.patient.name").value(reservation.getPatient().getName()))
                .andExpect(jsonPath("$.data.patient.address").value(reservation.getPatient().getAddress()))
                .andExpect(jsonPath("$.data.patient.phone").value(reservation.getPatient().getPhone()))
                .andExpect(jsonPath("$.data.patient.gender").value(reservation.getPatient().getGender().name()))
                .andExpect(jsonPath("$.data.doctor.name").value(doctor.getName()))
                .andExpect(jsonPath("$.data.doctor.medicalDepartment").value(doctor.getMedicalDepartment()));
    }
}
