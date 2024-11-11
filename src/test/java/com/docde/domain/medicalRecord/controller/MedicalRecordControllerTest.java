package com.docde.domain.medicalRecord.controller;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.config.JwtAuthenticationToken;
import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.medicalRecord.dto.request.DoctorMedicalRecordRequestDto;
import com.docde.domain.medicalRecord.dto.response.DoctorMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.MedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.PatientMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.service.MedicalRecordService;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwt;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicalRecordController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
public class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @MockBean
    private MedicalRecordService medicalRecordService;

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
                .name("name")
                .description("description")
                .hospital(hospital)
                .build();
        ReflectionTestUtils.setField(doctor, "id", 1L);

        patient = Patient.builder()
                .name("patientName")
                .address("address")
                .phone("111-1111")
                .gender(Gender.M)
                .build();
        ReflectionTestUtils.setField(patient, "id", 1L);

        user = new User("test@email.com", "password", UserRole.ROLE_DOCTOR, doctor, patient);
        ReflectionTestUtils.setField(user, "id", 1L);
    }


    @Nested
    class 진료기록_생성_컨트롤러 {

        private final LocalDateTime consultationTime = LocalDateTime.now();

        @Test
        @DisplayName("진료기록 생성 - 성공")
        public void test1() throws Exception {

            AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.ROLE_DOCTOR,
                    1L,
                    1L,
                    1L
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            // 요청 DTO
            DoctorMedicalRecordRequestDto requestDto = new DoctorMedicalRecordRequestDto(
                    1L,
                    1L,
                    "test description",
                    consultationTime,
                    "test treatment",
                    "test doctor comment"
            );


            // 예상 응답 DTO
            DoctorMedicalRecordResponseDto doctorRecord = new DoctorMedicalRecordResponseDto(
                    1L,
                    "test description",
                    consultationTime,
                    "Patient A",
                    1L,
                    "test treatmentPlan",
                    "test doctor comment"
            );

            PatientMedicalRecordResponseDto patientRecord = new PatientMedicalRecordResponseDto(
                    1L,
                    "Initial consultation",
                    consultationTime,
                    "Doctor A"
            );

            MedicalRecordResponseDto responseDto = new MedicalRecordResponseDto(doctorRecord, patientRecord);

            Mockito.when(medicalRecordService.createMedicalRecord(Mockito.any(), Mockito.any()))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/medical-records")
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto))
                            .with(csrf()))
                    .andExpect(status().isCreated());

        }


        @Nested
        @DisplayName("특정 진료기록 조회 - 성공")
        class GetSpecificDoctorMedicalRecordTest {

            @Test
            @DisplayName("특정 진료기록 조회 - 성공")
            public void getSpecificDoctorMedicalRecord_success() throws Exception {

                AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.ROLE_DOCTOR,
                        1L, 1L, 1L);
                JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

                DoctorMedicalRecordResponseDto responseDto = new DoctorMedicalRecordResponseDto(
                        1L, "test description", LocalDateTime.now(),
                        "Patient A", 1L, "test treatmentPlan", "test doctor comment");

                Mockito.when(medicalRecordService.getSpecificDoctorMedicalRecord(
                                Mockito.any(), Mockito.eq(1L), Mockito.eq("test description"),
                                Mockito.eq("test treatmentPlan"), Mockito.eq("test doctor comment")))
                        .thenReturn(responseDto);

                mockMvc.perform(get("/doctors/medical-records/{medicalRecordId}", 1L)
                                .with(authentication(authToken)) // 인증 정보 추가
                                .param("description", "test description")
                                .param("treatmentPlan", "test treatmentPlan")
                                .param("doctorComment", "test doctor comment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.medicalRecordId").value(1L))
                        .andExpect(jsonPath("$.data.description").value("test description"))
                        .andExpect(jsonPath("$.data.patientName").value("Patient A"))
                        .andExpect(jsonPath("$.data.treatmentPlan").value("test treatmentPlan"))
                        .andExpect(jsonPath("$.data.doctorComment").value("test doctor comment"));            }
        }


        @Test
        @DisplayName("의사 진료기록 목록 조회 - 성공")
        public void getDoctorMedicalRecord_success() throws Exception {

            AuthUser authUser = new AuthUser(1L, "doctor@email.com", UserRole.ROLE_DOCTOR,
                    1L, null, null);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            List<DoctorMedicalRecordResponseDto> responseDto = List.of(
                    new DoctorMedicalRecordResponseDto(1L, "description 1", LocalDateTime.now(),
                            "Patient A", 1L, "treatment plan 1", "doctor comment 1"),
                    new DoctorMedicalRecordResponseDto(2L, "description 2", LocalDateTime.now(),
                            "Patient B", 2L, "treatment plan 2", "doctor comment 2")
            );

            Mockito.when(medicalRecordService.getDoctorMedicalRecord(Mockito.any()))
                    .thenReturn(responseDto);

            mockMvc.perform(get("/doctors/medical-records")
                            .with(authentication(authToken)) // 인증 정보 추가
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].medicalRecordId").value(1L))
                    .andExpect(jsonPath("$.data[0].description").value("description 1"))
                    .andExpect(jsonPath("$.data[0].patientName").value("Patient A"))
                    .andExpect(jsonPath("$.data[0].treatmentPlan").value("treatment plan 1"))
                    .andExpect(jsonPath("$.data[0].doctorComment").value("doctor comment 1"))
                    .andExpect(jsonPath("$.data[1].medicalRecordId").value(2L))
                    .andExpect(jsonPath("$.data[1].description").value("description 2"))
                    .andExpect(jsonPath("$.data[1].patientName").value("Patient B"))
                    .andExpect(jsonPath("$.data[1].treatmentPlan").value("treatment plan 2"))
                    .andExpect(jsonPath("$.data[1].doctorComment").value("doctor comment 2"));        }


        @Test
        @DisplayName("환자 진료기록 목록 조회 - 성공")
        public void getPatientMedicalRecord_success() throws Exception {

            AuthUser authUser = new AuthUser(1L, "patient@email.com", UserRole.ROLE_PATIENT,
                    null, 1L, null);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            List<PatientMedicalRecordResponseDto> responseDtos = List.of(
                    new PatientMedicalRecordResponseDto(1L, "description 1", LocalDateTime.now(), "Doctor A"),
                    new PatientMedicalRecordResponseDto(2L, "description 2", LocalDateTime.now(), "Doctor B")
            );

            Mockito.when(medicalRecordService.getPatientMedicalRecord(Mockito.any()))
                    .thenReturn(responseDtos);

            mockMvc.perform(get("/patients/medical-records")
                            .with(authentication(authToken)) // 인증 정보 추가
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].medicalRecordId").value(1L))
                    .andExpect(jsonPath("$.data[0].description").value("description 1"))
                    .andExpect(jsonPath("$.data[0].doctorName").value("Doctor A"))
                    .andExpect(jsonPath("$.data[1].medicalRecordId").value(2L))
                    .andExpect(jsonPath("$.data[1].description").value("description 2"))
                    .andExpect(jsonPath("$.data[1].doctorName").value("Doctor B"));
        }

        @Test
        @DisplayName("진료기록 수정 - 성공")
        public void updateMedicalRecord_success() throws Exception {

            AuthUser authUser = new AuthUser(1L, "doctor@email.com", UserRole.ROLE_DOCTOR,
                    1L, null, null);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            DoctorMedicalRecordRequestDto requestDto = new DoctorMedicalRecordRequestDto(
                    1L, 1L, "updated description", LocalDateTime.now(), "updated treatment plan", "updated doctor comment");

            DoctorMedicalRecordResponseDto doctorResponse = new DoctorMedicalRecordResponseDto(
                    1L, "updated description", LocalDateTime.now(),
                    "Patient A", 1L, "updated treatment plan", "updated doctor comment");

            PatientMedicalRecordResponseDto patientResponse = new PatientMedicalRecordResponseDto(
                    1L, "updated description", LocalDateTime.now(), "Doctor A");

            MedicalRecordResponseDto responseDto = new MedicalRecordResponseDto(doctorResponse, patientResponse);

            Mockito.when(medicalRecordService.updateMedicalRecord(Mockito.eq(1L), Mockito.any(), Mockito.any()))
                    .thenReturn(responseDto);

            mockMvc.perform(put("/doctors/medical-records/{medicalRecordId}", 1L)
                            .with(authentication(authToken)) // 인증 정보 추가
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto)) // 요청 본문에 JSON 데이터 추가
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("진료기록 삭제 - 성공")
        public void deleteMedicalRecord_success() throws Exception {

            AuthUser authUser = new AuthUser(1L, "doctor@email.com", UserRole.ROLE_DOCTOR,
                    1L, null, null);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            Mockito.doNothing().when(medicalRecordService).deleteMedicalRecord(Mockito.eq(1L), Mockito.any());

            mockMvc.perform(delete("/medical-records/{medicalRecordId}", 1L)
                            .with(authentication(authToken)) // 인증 정보 추가
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }
}
