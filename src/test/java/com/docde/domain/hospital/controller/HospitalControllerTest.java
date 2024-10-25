package com.docde.domain.hospital.controller;

import com.docde.common.enums.UserRole;
import com.docde.config.JwtUtil;
import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.auth.service.UserDetailsServiceImpl;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.response.HospitalPostResponseDto;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.service.HospitalService;
import com.docde.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = HospitalController.class)
public class HospitalControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HospitalService hospitalService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    ObjectMapper objectMapper;


    Doctor doctor = new Doctor("president_lee", "test");
    User user = new User(
            "testemail",
            "password",
            UserRole.ROLE_DOCTOR_PRESIDENT,
            doctor,
            null
    );
    UserDetailsImpl userDetails = new UserDetailsImpl(user);
    HospitalPostRequestDto requestDto = new HospitalPostRequestDto(
            "testHospitalName",
            "testHospitalAddress",
            "testHospitalContact",
            LocalTime.now(),
            LocalTime.now().minusHours(3),
            "testannouncement"
    );

    @Test
    @DisplayName("병원 정보 생성 성공")
    @WithMockUser
    void test1() throws Exception {
        //G
        Long hospitalId = 1L;
        Hospital hospital = new Hospital(requestDto);
        ReflectionTestUtils.setField(hospital, "Id", hospitalId);

        HospitalPostResponseDto responseDto = new HospitalPostResponseDto(hospital);

        String requestContent = objectMapper.writeValueAsString(requestDto);
        String content = objectMapper.writeValueAsString(responseDto);

        when(hospitalService.postHospital(any(HospitalPostRequestDto.class), any(UserDetailsImpl.class))).thenReturn(responseDto);
        MvcResult result = mockMvc.perform(post("/hospitals")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().json(content)).andReturn();

        System.out.println(result.getResponse().getContentAsString());


    }
}
