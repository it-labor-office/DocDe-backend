package com.docde.domain.hospital.controller;

import com.docde.common.enums.UserRole;
import com.docde.config.JwtAuthenticationToken;
import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.hospital.dto.request.*;
import com.docde.domain.hospital.dto.response.*;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.service.HospitalService;
import com.docde.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HospitalController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
public class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private HospitalService hospitalService;

    HospitalPostRequestDto requestDto = new HospitalPostRequestDto(
            "testname",
            "testadress",
            "testContact",
            LocalTime.now(),
            LocalTime.now().plusHours(9),
            "test"
    );
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Nested
    class 병원_생성_컨트롤러 {
        @Test
        @DisplayName("권한이 doctor_president인 경우 201")
        public void test1() throws Exception {
            AuthUser authUser = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, null);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            Mockito.when(hospitalService.postHospital(requestDto, authUser)).thenReturn(new HospitalPostResponseDto(hospital));
            mockMvc.perform(post("/hospitals")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jacksonObjectMapper.writeValueAsString(requestDto))
                    .with(csrf())).andExpect(status().isCreated());

        }

        @Test
        @DisplayName("권한이 doctor여서 실패")
        public void test2() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);

            AuthUser doctor = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR, null, null, null);
            JwtAuthenticationToken doctorToken = new JwtAuthenticationToken(doctor);

            Mockito.when(hospitalService.postHospital(requestDto, doctor)).thenReturn(new HospitalPostResponseDto(hospital));
            mockMvc.perform(post("/hospitals")
                    .with(authentication(doctorToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jacksonObjectMapper.writeValueAsString(requestDto))
                    .with(csrf())).andExpect(status().isForbidden());
        }
    }

    @Nested
    public class 병원_추가_컨트롤러 {

        @Test
        @DisplayName("해당 병원 병원장이 의사를 추가")
        public void test3() throws Exception {
            //병원 하나 생성
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            //병원장 생성 병원장은 병원 Id 1 에 소속
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 1L);
            //추가 할 의사 생성
            Doctor doctor = new Doctor();
            ReflectionTestUtils.setField(doctor, "id", 1L);
            ReflectionTestUtils.setField(doctor, "name", "test");
            User user = new User();
            ReflectionTestUtils.setField(user, "email", "doctorEmail");
            ReflectionTestUtils.setField(doctor, "user", user);

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);
            HospitalPostDoctorRequestDto requestDto1 = new HospitalPostDoctorRequestDto("doctorEmail");

            Mockito.when(hospitalService.addDoctorToHospital(hospital.getId(), requestDto1, president)).
                    thenReturn(new HospitalPostDoctorResponseDto(doctor.getId(), doctor.getName(), hospital.getId(), hospital.getName()));
            mockMvc.perform(post("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("해당 병원장이 아닌 병원장이 의사를 추가하려다가 실패")
        public void test4() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            //병원장 생성 병원장은 병원 Id 2 에 소속
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 2L);
            //추가 할 의사 생성
            Doctor doctor = new Doctor();
            ReflectionTestUtils.setField(doctor, "id", 1L);
            ReflectionTestUtils.setField(doctor, "name", "test");
            User user = new User();
            ReflectionTestUtils.setField(user, "email", "doctorEmail");
            ReflectionTestUtils.setField(doctor, "user", user);

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);
            HospitalPostDoctorRequestDto requestDto1 = new HospitalPostDoctorRequestDto("doctorEmail");

            Mockito.when(hospitalService.addDoctorToHospital(hospital.getId(), requestDto1, president)).
                    thenReturn(new HospitalPostDoctorResponseDto(doctor.getId(), doctor.getName(), hospital.getId(), hospital.getName()));
            mockMvc.perform(post("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("병원장이 아닌 사람이 의사를 추가하려다가 실패")
        public void test5() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            //병원장 생성 병원장은 병원 Id 1 에 소속
            AuthUser notPresident = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR, null, null, 1L);
            //추가 할 의사 생성
            Doctor doctor = new Doctor();
            ReflectionTestUtils.setField(doctor, "id", 1L);
            ReflectionTestUtils.setField(doctor, "name", "test");
            User user = new User();
            ReflectionTestUtils.setField(user, "email", "doctorEmail");
            ReflectionTestUtils.setField(doctor, "user", user);

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(notPresident);
            HospitalPostDoctorRequestDto requestDto1 = new HospitalPostDoctorRequestDto("doctorEmail");

            Mockito.when(hospitalService.addDoctorToHospital(hospital.getId(), requestDto1, notPresident)).
                    thenReturn(new HospitalPostDoctorResponseDto(doctor.getId(), doctor.getName(), hospital.getId(), hospital.getName()));
            mockMvc.perform(post("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    public class 병원_정보_읽어오기 {

        @Test
        @DisplayName("병원 정보 불러오기 정상 작동")
        public void test6() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);

            AuthUser patient = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_PATIENT, null, null, null);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(patient);

            mockMvc.perform(get("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    public class 병원_정보_수정하기 {

        @Test
        @DisplayName("병원 정보 수정 성공")
        public void test7() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 1L);
            HospitalUpdateRequestDto requestDto1 = new HospitalUpdateRequestDto(
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);
            Mockito.when(hospitalService.patchHospital(requestDto1, hospital.getId(), president)).thenReturn(new HospitalUpdateResponseDto());
            mockMvc.perform(put("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("해당 병원 병원장이 아닌 사람이 수정하려다가 실패")
        public void test8() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 2L);
            HospitalUpdateRequestDto requestDto1 = new HospitalUpdateRequestDto(
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);
            Mockito.when(hospitalService.patchHospital(requestDto1, hospital.getId(), president)).thenReturn(new HospitalUpdateResponseDto());
            mockMvc.perform(put("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("해당 병원 사람은 맞지만 병원장이 아니여서 실패")
        public void test9() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser doctor = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR, null, null, 1L);
            HospitalUpdateRequestDto requestDto1 = new HospitalUpdateRequestDto(
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(doctor);
            Mockito.when(hospitalService.patchHospital(requestDto1, hospital.getId(), doctor)).thenReturn(new HospitalUpdateResponseDto());
            mockMvc.perform(put("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("병원 수정 성공_Patch")
        public void test9_patch() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser doctor = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 1L);
            HospitalUpdateRequestDto requestDto1 = new HospitalUpdateRequestDto(
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(doctor);
            Mockito.when(hospitalService.patchHospital(requestDto1, hospital.getId(), doctor)).thenReturn(new HospitalUpdateResponseDto());
            mockMvc.perform(put("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("병원 수정 실패_Patch 병원장 권한이 없어 실패")
        public void test10_patch() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser doctor = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR, null, null, 1L);
            HospitalUpdateRequestDto requestDto1 = new HospitalUpdateRequestDto(
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(doctor);
            Mockito.when(hospitalService.patchHospital(requestDto1, hospital.getId(), doctor)).thenReturn(new HospitalUpdateResponseDto());
            mockMvc.perform(put("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("병원 수정 실패_Patch 본인 병원이 아니여서 실패")
        public void test11_patch() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser doctor = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 2L);
            HospitalUpdateRequestDto requestDto1 = new HospitalUpdateRequestDto(
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(doctor);
            Mockito.when(hospitalService.patchHospital(requestDto1, hospital.getId(), doctor)).thenReturn(new HospitalUpdateResponseDto());
            mockMvc.perform(put("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    public class 병원_시간표_생성 {

        @Test
        @DisplayName("병원 시간표 정상 생성 완료")
        public void test10() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 1L);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);
            HospitalWeeklyTimetablePostRequestDto requestDto1 = new HospitalWeeklyTimetablePostRequestDto();

            Mockito.when(hospitalService.postWeeklyTimetable(requestDto1, president, hospital.getId())).thenReturn(new HospitalWeeklyTimetablePostResponseDto());
            mockMvc.perform(post("/hospitals/{hospitalId}/time-table", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("해당 병원 사람이지만 병원장이 아니여서 실패")
        public void test11() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR, null, null, 1L);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);
            HospitalWeeklyTimetablePostRequestDto requestDto1 = new HospitalWeeklyTimetablePostRequestDto();

            Mockito.when(hospitalService.postWeeklyTimetable(requestDto1, president, hospital.getId())).thenReturn(new HospitalWeeklyTimetablePostResponseDto());
            mockMvc.perform(post("/hospitals/{hospitalId}/time-table", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("병원장 이지만 해당 병원 병원장이 아님 때문에 실패")
        public void test12() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR, null, null, 2L);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);
            HospitalWeeklyTimetablePostRequestDto requestDto1 = new HospitalWeeklyTimetablePostRequestDto();

            Mockito.when(hospitalService.postWeeklyTimetable(requestDto1, president, hospital.getId())).thenReturn(new HospitalWeeklyTimetablePostResponseDto());
            mockMvc.perform(post("/hospitals/{hospitalId}/time-table", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    public class 병원_시간표_수정 {
        @Test
        @DisplayName("병원 시간표 수정 성공_Put")
        public void test13() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 1L);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);

            HospitalWeeklyTimetableUpdateRequestDto requestDto1 = new HospitalWeeklyTimetableUpdateRequestDto();

            Mockito.when(hospitalService.updateWeeklyTimetable(requestDto1, president, hospital.getId())).thenReturn(new HospitalWeeklyTimetableUpdateResponseDto());
            mockMvc.perform(patch("/hospitals/{hospitalId}/time-table", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("병원장이 아니여서 수정 실패_Put")
        public void test14() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR, null, null, 1L);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);

            HospitalWeeklyTimetableUpdateRequestDto requestDto1 = new HospitalWeeklyTimetableUpdateRequestDto();

            Mockito.when(hospitalService.updateWeeklyTimetable(requestDto1, president, hospital.getId())).thenReturn(new HospitalWeeklyTimetableUpdateResponseDto());
            mockMvc.perform(patch("/hospitals/{hospitalId}/time-table", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("해당 병원 병원장이 아니여서 수정 실패_Put")
        public void test15() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 2L);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);

            HospitalWeeklyTimetableUpdateRequestDto requestDto1 = new HospitalWeeklyTimetableUpdateRequestDto();

            Mockito.when(hospitalService.updateWeeklyTimetable(requestDto1, president, hospital.getId())).thenReturn(new HospitalWeeklyTimetableUpdateResponseDto());
            mockMvc.perform(patch("/hospitals/{hospitalId}/time-table", hospital.getId())
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto1))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("병원 삭제")
    public class DeleteHospital {
        @Test
        @DisplayName("병원 삭제 성공")
        public void test16() throws Exception {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            AuthUser president = new AuthUser(1L, "test@gmail.com",
                    UserRole.ROLE_DOCTOR_PRESIDENT, null, null, 1L);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(president);


            Mockito.when(hospitalService.deleteHospital(president)).thenReturn(new HospitalDeleteResponseDto());
            mockMvc.perform(delete("/hospitals/{hospitalId}", hospital.getId())
                            .with(authentication(authToken))
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }
}
