package com.docde.domain.auth.controller;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.config.JwtUtil;
import com.docde.domain.auth.dto.AuthRequest;
import com.docde.domain.auth.dto.AuthResponse;
import com.docde.domain.auth.service.AuthService;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("/auth/signup/patient")
    @WithMockUser
    void patientSignUp() throws Exception {
        // given
        String email = "a@a.com";
        String password = "Password1234@";
        String name = "dlfma";
        String address = "wnth";
        String phone = "01012345678";
        Gender gender = Gender.M;
        String code = "code";
        Long patientId = 1L;
        Long userId = 1L;
        AuthRequest.PatientSignUp patientSignUp = new AuthRequest.PatientSignUp(email, password, name, address, phone, gender, code);
        Patient patient = Patient.builder().name(name).address(address).phone(phone).gender(gender).build();
        User user = User.builder().email(email).password(password).userRole(UserRole.ROLE_PATIENT).patient(patient).build();
        ReflectionTestUtils.setField(patient, "id", patientId);
        ReflectionTestUtils.setField(user, "id", userId);
        String content = objectMapper.writeValueAsString(patientSignUp);

        // when
        when(authService.patientSignUp(email, password, name, address, phone, gender, code)).thenReturn(user);

        // then
        mockMvc.perform(post("/auth/signup/patient")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.userRole").value("ROLE_PATIENT"))
                .andExpect(jsonPath("$.data.patient.name").value(name))
                .andExpect(jsonPath("$.data.patient.address").value(address))
                .andExpect(jsonPath("$.data.patient.phone").value(phone))
                .andExpect(jsonPath("$.data.patient.gender").value("M"));
    }


    @Test
    @DisplayName("/auth/signup/doctor")
    @WithMockUser
    void doctorSignUp() throws Exception {
        // given
        String email = "a@a.com";
        String password = "Password1234@";
        String name = "dlfma";
        String description = "tkdtp";
        String code = "code";
        boolean isDoctorPresident = false;
        Long doctorId = 1L;
        Long userId = 1L;
        AuthRequest.DoctorSignUp doctorSignUp = new AuthRequest.DoctorSignUp(email, password, name, description, isDoctorPresident, code);
        Doctor doctor = Doctor.builder().name(name).description(description).build();
        User user = User.builder().email(email).password(password).userRole(UserRole.ROLE_DOCTOR).doctor(doctor).build();
        ReflectionTestUtils.setField(doctor, "id", doctorId);
        ReflectionTestUtils.setField(user, "id", userId);
        when(authService.doctorSignUp(email, password, name, description, isDoctorPresident, code)).thenReturn(user);
        String content = objectMapper.writeValueAsString(doctorSignUp);

        // when & then
        mockMvc.perform(post("/auth/signup/doctor")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.userRole").value("ROLE_DOCTOR"))
                .andExpect(jsonPath("$.data.doctor.name").value(name))
                .andExpect(jsonPath("$.data.doctor.description").value(description));
    }

    @Test
    @DisplayName("/auth/refresh")
    @WithMockUser
    void reissue() throws Exception {
        // given
        String refreshToken = "refreshToken";
        AuthRequest.ReissueToken reissueToken = new AuthRequest.ReissueToken(refreshToken);
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        AuthResponse.SignIn signIn = new AuthResponse.SignIn(newAccessToken, newRefreshToken);
        when(authService.reissueToken(refreshToken)).thenReturn(signIn);
        String content = objectMapper.writeValueAsString(reissueToken);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(newRefreshToken));
    }

    @Test
    @DisplayName("/auth/email-authentication")
    @WithMockUser
    void authenticateEmail() throws Exception {
        // given
        String email = "a@a.com";
        AuthRequest.AuthenticateEmail authenticateEmailRequestDto = new AuthRequest.AuthenticateEmail(email);
        String content = objectMapper.writeValueAsString(authenticateEmailRequestDto);

        // when & then
        mockMvc.perform(post("/auth/email-authentication")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Created"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }
}
