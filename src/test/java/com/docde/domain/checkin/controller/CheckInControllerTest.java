package com.docde.domain.checkin.controller;

import com.docde.common.enums.UserRole;
import com.docde.config.JwtAuthenticationToken;
import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.dto.CheckInResponseOfPatient;
import com.docde.domain.checkin.service.CheckInService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    AuthUser patientAuthUser = new AuthUser(
            1L,
            "e@ma.il",
            UserRole.ROLE_PATIENT,
            null,
            1L,
            null
    );
    JwtAuthenticationToken patientToken = new JwtAuthenticationToken(patientAuthUser);

    AuthUser doctorAuthUser = new AuthUser(
            2L,
            "e2@ma.il",
            UserRole.ROLE_DOCTOR_PRESIDENT,
            1L,
            null,
            1L
    );
    JwtAuthenticationToken doctorToken = new JwtAuthenticationToken(doctorAuthUser);

    CheckInRequest checkInRequest = new CheckInRequest(1L, null);

    CheckInResponse checkInResponse = new CheckInResponse(
            1L,
            "patientName",
            null,
            LocalDateTime.now(),
            "WAITING"
    );

    CheckInResponseOfPatient checkInResponseOfPatient = new CheckInResponseOfPatient(
            "patientName",
            "doctorName",
            LocalDateTime.now(),
            1L
    );

    @Test
    void saveCheckIn() throws Exception {

        Mockito.when(checkInService.saveCheckIn(patientAuthUser, 1L, checkInRequest))
                .thenReturn(checkInResponse);

        mockMvc.perform(post("/hospitals/1/checkin")
                .with(SecurityMockMvcRequestPostProcessors.authentication(patientToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper.writeValueAsString(checkInRequest))
                .with(csrf())
        ).andExpect(status().isCreated());
    }

    @Test
    void getMyCheckIn() throws Exception {

        Mockito.when(checkInService.getMyCheckIn(patientAuthUser, 1L)).thenReturn(checkInResponseOfPatient);

        mockMvc.perform(get("/hospitals/1/checkin")
                .with(authentication(patientToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper.writeValueAsString(checkInRequest))
                .with(csrf())
        ).andExpect(status().isOk());
    }

    @Test
    void getQueue() throws Exception {

        List<Object> queue = new ArrayList<>();
        Mockito.when(checkInService.getQueue(doctorAuthUser, 1L)).thenReturn(queue);

        mockMvc.perform(get("/hospitals/1/checkin/simple")
                .with(authentication(patientToken))
                .with(csrf())
        ).andExpect(status().isOk());
    }

    @Test
    void getAllCheckIns() throws Exception {

        List<CheckInResponse> checkInResponseList = new ArrayList<>();
        Mockito.when(checkInService.getAllCheckIns(doctorAuthUser, 1L)).thenReturn(checkInResponseList);

        mockMvc.perform(get("/hospitals/1/checkin/all")
                .with(authentication(patientToken))
                .with(csrf())
        ).andExpect(status().isOk());
    }

    @Test
    void updateCheckIn() throws Exception {

        Mockito.when(checkInService.updateCheckIn(doctorAuthUser, 1L, 1L, checkInRequest))
                .thenReturn(checkInResponse);

        mockMvc.perform(put("/hospitals/1/checkin/1")
                .with(authentication(patientToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper.writeValueAsString(checkInRequest))
                .with(csrf())
        ).andExpect(status().isOk());
    }

    @Test
    void resetNumber() throws Exception {

        mockMvc.perform(put("/hospitals/1/checkin/reset")
                .with(authentication(doctorToken))
                .with(csrf())
        ).andExpect(status().isOk());
    }

    @Test
    void deleteCheckIn() throws Exception {

        mockMvc.perform(delete("/hospitals/checkin/1")
                .with(authentication(doctorToken))
                .with(csrf())
        ).andExpect(status().isOk());
    }
}