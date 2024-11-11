package com.docde.domain.reservation.controller;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.config.JwtAuthenticationToken;
import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.patient.dto.PatientResponse;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.reservation.contorller.ReservationPatientController;
import com.docde.domain.reservation.dto.ReservationPatientRequest;
import com.docde.domain.reservation.dto.ReservationPatientResponse;
import com.docde.domain.reservation.entity.Reservation;
import com.docde.domain.reservation.entity.ReservationStatus;
import com.docde.domain.reservation.service.ReservationPatientService;
import com.docde.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationPatientController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
public class ReservationPatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @MockBean
    private ReservationPatientService reservationPatientService;

    @MockBean
    private JwtUtil jwtUtil;

    @Mock
    private Hospital hospital;

    @Mock
    private Doctor doctor;

    @Mock
    private Patient patient;

    @Mock
    private User user;

    @BeforeEach
    public void setup() {

        hospital = Hospital.builder()
                .name("Test Hospital")
                .address("hospital address")
                .contact("111-2222")
                .open_time(LocalTime.of(9, 0))
                .closing_time(LocalTime.of(17, 0))
                .announcement("announcement")
                .build();
        ReflectionTestUtils.setField(hospital, "id", 1L);

        doctor = Doctor.builder()
                .name("doctor name")
                .description("description")
                .hospital(hospital)
                .build();
        ReflectionTestUtils.setField(doctor, "id", 1L);

        patient = Patient.builder()
                .name("patient name")
                .address("address")
                .phone("111-1111")
                .gender(Gender.M)
                .build();
        ReflectionTestUtils.setField(patient, "id", 1L);

        user = new User("test@email.com", "password", UserRole.ROLE_DOCTOR, doctor, patient);
        ReflectionTestUtils.setField(user, "id", 1L);
    }


    @Nested
    class 환자예약_컨트롤러_테스트 {


        @Test
        @DisplayName("예약 생성 - 성공")
        public void createReservation_success() throws Exception {

            AuthUser authUser = new AuthUser(1L, "patient@email.com", UserRole.ROLE_PATIENT, null, 1L, null);

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            ReservationPatientRequest.CreateReservation requestDto = new ReservationPatientRequest.CreateReservation(
                    "reservationReason", doctor.getId(), LocalDateTime.now().plusDays(1));

            LocalDateTime reservationTime = LocalDateTime.now().plusDays(1);

            PatientResponse patientResponse = new PatientResponse(patient.getId(), patient.getName(), patient.getAddress(), patient.getPhone(), patient.getGender());

            DoctorResponse doctorResponse = new DoctorResponse(doctor.getId(), doctor.getName(), doctor.getDescription());

            ReservationPatientResponse.ReservationWithPatientAndDoctor responseDto = new ReservationPatientResponse.ReservationWithPatientAndDoctor(
                    1L, "reservationReason", ReservationStatus.WAITING_RESERVATION, null, patientResponse, doctorResponse);

            // 예약 엔티티 생성
            Reservation reservation = Reservation.builder()
                    .reservationReason(responseDto.reservationReason())
                    .status(responseDto.status())
                    .rejectReason(responseDto.rejectReason())
                    .reservationTime(reservationTime)
                    .doctor(doctor)
                    .patient(patient)
                    .build();

            ReflectionTestUtils.setField(reservation, "id", 1L);

            Mockito.when(reservationPatientService.createReservation(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(reservation);


            mockMvc.perform(post("/reservations")
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(responseDto.id()))
                    .andExpect(jsonPath("$.data.reservationReason").value("reservationReason"))
                    .andExpect(jsonPath("$.data.status").value("WAITING_RESERVATION"))
                    .andExpect(jsonPath("$.data.patient.name").value("patient name"))
                    .andExpect(jsonPath("$.data.patient.address").value("address"))
                    .andExpect(jsonPath("$.data.patient.gender").value("M"))
                    .andExpect(jsonPath("$.data.doctor.name").value("doctor name"))
                    .andExpect(jsonPath("$.data.doctor.description").value("description"));
        }
    }

    @Test
    @DisplayName("예약 취소 - 성공")
    public void cancelReservation_success() throws Exception {

        AuthUser authUser = new AuthUser(1L, "patient@email.com", UserRole.ROLE_PATIENT, null, 1L, null);

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

        Long reservationId = 1L;

        LocalDateTime reservationTime = LocalDateTime.now().plusDays(1);

        Reservation reservation = Reservation.builder()
                .reservationReason("reservationReason")
                .status(ReservationStatus.WAITING_RESERVATION)
                .reservationTime(reservationTime)
                .doctor(doctor)
                .patient(patient)
                .build();
        ReflectionTestUtils.setField(reservation, "id", reservationId);
        reservation.setStatus(ReservationStatus.RESERVATION_CANCELED); // 예약 상태를 취소로 변경

        PatientResponse patientResponse = new PatientResponse(patient.getId(), patient.getName(), patient.getAddress(), patient.getPhone(), patient.getGender());
        DoctorResponse doctorResponse = new DoctorResponse(doctor.getId(), doctor.getName(), doctor.getDescription());
        ReservationPatientResponse.ReservationWithPatientAndDoctor responseDto = new ReservationPatientResponse.ReservationWithPatientAndDoctor(
                reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), patientResponse, doctorResponse);

        Mockito.when(reservationPatientService.cancelReservation(Mockito.eq(reservationId), Mockito.any()))
                .thenReturn(reservation);

        mockMvc.perform(put("/reservations/{reservationId}/cancel", reservationId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(responseDto.id()))
                .andExpect(jsonPath("$.data.reservationReason").value("reservationReason"))
                .andExpect(jsonPath("$.data.status").value("RESERVATION_CANCELED"))
                .andExpect(jsonPath("$.data.patient.name").value(patient.getName()))
                .andExpect(jsonPath("$.data.patient.address").value(patient.getAddress()))
                .andExpect(jsonPath("$.data.patient.gender").value(patient.getGender().toString()))
                .andExpect(jsonPath("$.data.doctor.name").value(doctor.getName()))
                .andExpect(jsonPath("$.data.doctor.description").value(doctor.getDescription()));
    }

    @Test
    @DisplayName("예약 상세 조회 - 성공")
    public void getReservation_success() throws Exception {

        AuthUser authUser = new AuthUser(1L, "patient@email.com", UserRole.ROLE_PATIENT, null, 1L, null);
        JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

        Long reservationId = 1L;

        LocalDateTime reservationTime = LocalDateTime.now().plusDays(1);
        Reservation reservation = Reservation.builder()
                .reservationReason("reservationReason")
                .status(ReservationStatus.WAITING_RESERVATION)
                .reservationTime(reservationTime)
                .doctor(doctor)
                .patient(patient)
                .build();
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        PatientResponse patientResponse = new PatientResponse(patient.getId(), patient.getName(), patient.getAddress(), patient.getPhone(), patient.getGender());
        DoctorResponse doctorResponse = new DoctorResponse(doctor.getId(), doctor.getName(), doctor.getDescription());
        ReservationPatientResponse.ReservationWithPatientAndDoctor responseDto = new ReservationPatientResponse.ReservationWithPatientAndDoctor(
                reservation.getId(), reservation.getReservationReason(), reservation.getStatus(), reservation.getRejectReason(), patientResponse, doctorResponse);

        Mockito.when(reservationPatientService.getReservation(Mockito.eq(reservationId), Mockito.any()))
                .thenReturn(reservation);

        mockMvc.perform(get("/reservations/{reservationId}", reservationId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(responseDto.id()))
                .andExpect(jsonPath("$.data.reservationReason").value("reservationReason"))
                .andExpect(jsonPath("$.data.status").value("WAITING_RESERVATION"))
                .andExpect(jsonPath("$.data.patient.name").value(patient.getName()))
                .andExpect(jsonPath("$.data.patient.address").value(patient.getAddress()))
                .andExpect(jsonPath("$.data.patient.gender").value(patient.getGender().toString()))
                .andExpect(jsonPath("$.data.doctor.name").value(doctor.getName()))
                .andExpect(jsonPath("$.data.doctor.description").value(doctor.getDescription()));
    }

}
