package com.docde.domain.checkin.controller;

import com.docde.common.enums.UserRole;
import com.docde.config.JwtAuthenticationToken;
import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.auth.service.AuthService;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.service.CheckInService;
import com.docde.domain.hospital.controller.HospitalController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckInController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @MockBean
    private CheckInService checkInService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void saveCheckIn() throws Exception {
        AuthUser patientAuthUser = new AuthUser(
                1L,
                "e@ma.il",
                UserRole.ROLE_PATIENT,
                null,
                1L,
                null
        );
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(patientAuthUser);

        CheckInRequest checkInRequest = new CheckInRequest();

        CheckInResponse checkInResponse = new CheckInResponse(
                1L,
                "patientName",
                null,
                LocalDateTime.now(),
                "WAITING"
        );

        Mockito.when(checkInService.saveCheckIn(patientAuthUser, 1L, checkInRequest))
                .thenReturn(checkInResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/hospitals/1/checkin")
                .with(SecurityMockMvcRequestPostProcessors.authentication(jwtAuthenticationToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper.writeValueAsString(checkInRequest))
                .with(csrf())
        ).andExpect(status().isCreated());
    }

    @Test
    void getMyCheckIn() {
    }

    @Test
    void getQueue() {
    }

    @Test
    void getAllCheckIns() {
    }

    @Test
    void updateCheckIn() {
    }

    @Test
    void resetNumber() {
    }

    @Test
    void deleteCheckIn() {
    }
}