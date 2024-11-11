package com.docde.domain.review.controller;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.config.JwtAuthenticationToken;
import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.review.dto.request.ReviewRequestDto;
import com.docde.domain.review.dto.request.ReviewUpdateRequestDto;
import com.docde.domain.review.dto.response.ReviewResponseDto;
import com.docde.domain.review.entity.Review;
import com.docde.domain.review.service.ReviewService;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    ReviewRequestDto requestDto = new ReviewRequestDto(
            1L,
            2L,
            3L,
            "contents"
    );

    @Nested
    class 리뷰_생성_컨트롤러 {
        @Test
        @DisplayName("진료기록 생성 - 성공")
        public void test1() throws Exception {
            AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.ROLE_PATIENT,
                    1L,
                    1L,
                    1L
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            Hospital hospital = Hospital.builder()
                    .name("Test Hospital")
                    .address("hospital address")
                    .contact("111-2222")
                    .open_time(LocalTime.of(9, 0))
                    .closing_time(LocalTime.of(17, 0))
                    .announcement("announcement")
                    .build();
            ReflectionTestUtils.setField(hospital, "id", 1L);


            Doctor doctor = Doctor.builder()
                    .name("name")
                    .description("description")
                    .hospital(hospital)
                    .build();
            ReflectionTestUtils.setField(doctor, "id", 1L);


            Patient patient = Patient.builder()
                    .name("patientName")
                    .address("address")
                    .phone("111-1111")
                    .gender(Gender.M)
                    .build();
            ReflectionTestUtils.setField(patient, "id", 1L);


            User user = new User("test@email.com", "password", UserRole.ROLE_PATIENT, doctor, patient);
            ReflectionTestUtils.setField(user, "id", 1L);


            MedicalRecord medicalRecord = new MedicalRecord(
                    "Description",
                    LocalDateTime.now(),
                    patient,
                    doctor,
                    "Test Plan",
                    "Doctor Comment"
            );

            ReflectionTestUtils.setField(medicalRecord, "medicalRecordId", 1L);


            Review review = new Review(3L, "so so service", user, medicalRecord);
            ReviewResponseDto responseDto = new ReviewResponseDto(
                    medicalRecord.getMedicalRecordId(),
                    user.getId(),
                    review.getStar(),
                    review.getContents(),
                    patient.getName()
            );

            Mockito.when(reviewService.createReview(Mockito.eq(authUser), Mockito.any(ReviewRequestDto.class))).thenReturn(responseDto);

            mockMvc.perform(post("/reviews")
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(requestDto))
                            .with(csrf()))
                    .andExpect(status().isCreated());

        }
    }

    @Nested
    public class 리뷰_조회_컨트롤러 {
        @Test
        @DisplayName("모든 리뷰 조회 - 성공")
        public void test2() throws Exception {
            AuthUser authUser = new AuthUser(1L, "testuser@gmail.com", UserRole.ROLE_PATIENT, null, null, null);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);
            List<ReviewResponseDto> reviewResponseDto = Arrays.asList(
                    new ReviewResponseDto(1L, 1L, 3L, "so so service", "test1"),
                    new ReviewResponseDto(2L, 2L, 4L, "Good service", "test2")
            );

            Mockito.when(reviewService.getAllReviews()).thenReturn(reviewResponseDto);

            mockMvc.perform(get("/reviews")
                            .with(authentication(authToken))  // 인증 정보 추가
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].medicalRecordId").value(1L))
                    .andExpect(jsonPath("$.data[0].userId").value(1L))
                    .andExpect(jsonPath("$.data[0].star").value(3L))
                    .andExpect(jsonPath("$.data[0].contents").value("so so service"))
                    .andExpect(jsonPath("$.data[0].patientName").value("test1"))
                    .andExpect(jsonPath("$.data[1].medicalRecordId").value(2L))
                    .andExpect(jsonPath("$.data[1].userId").value(2L))
                    .andExpect(jsonPath("$.data[1].star").value(4L))
                    .andExpect(jsonPath("$.data[1].contents").value("Good service"))
                    .andExpect(jsonPath("$.data[1].patientName").value("test2"));
        }
    }

    @Test
    @DisplayName("특정 사용자 리뷰 조회 - 성공")
    public void getReviewsByUserId_Success() throws Exception {
        // 인증된 사용자 생성 및 토큰 설정
        AuthUser authUser = new AuthUser(1L, "testuser@gmail.com", UserRole.ROLE_PATIENT, null, null, null);
        JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

        List<ReviewResponseDto> reviewResponseDtoList = Arrays.asList(
                new ReviewResponseDto(1L, 1L, 5L, "Great service!", "Patient A"),
                new ReviewResponseDto(2L, 1L, 4L, "Good experience", "Patient B")
        );

        Mockito.when(reviewService.getReviewsByUserId(1L)).thenReturn(reviewResponseDtoList);

        mockMvc.perform(get("/reviews/user/{userId}", 1L)
                        .with(authentication(authToken))  // 인증 정보 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].medicalRecordId").value(1L))
                .andExpect(jsonPath("$.data[0].userId").value(1L))
                .andExpect(jsonPath("$.data[0].star").value(5L))
                .andExpect(jsonPath("$.data[0].contents").value("Great service!"))
                .andExpect(jsonPath("$.data[0].patientName").value("Patient A"))
                .andExpect(jsonPath("$.data[1].medicalRecordId").value(2L))
                .andExpect(jsonPath("$.data[1].userId").value(1L))
                .andExpect(jsonPath("$.data[1].star").value(4L))
                .andExpect(jsonPath("$.data[1].contents").value("Good experience"))
                .andExpect(jsonPath("$.data[1].patientName").value("Patient B"));
    }


    @Nested
    public class 리뷰_수정_컨트롤러 {

        @Test
        @DisplayName("리뷰 수정 - 성공")
        public void updateReview_Success() throws Exception {

            Long reviewId = 1L;
            ReviewUpdateRequestDto updateRequestDto = new ReviewUpdateRequestDto(
                    4L,
                    "Updated review contents",
                    1L
            );

            AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.ROLE_PATIENT,
                    1L,
                    1L,
                    1L
            );

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            Hospital hospital = Hospital.builder()
                    .name("Test Hospital")
                    .address("hospital address")
                    .contact("111-2222")
                    .open_time(LocalTime.of(9, 0))
                    .closing_time(LocalTime.of(17, 0))
                    .announcement("announcement")
                    .build();
            ReflectionTestUtils.setField(hospital, "id", 1L);


            Doctor doctor = Doctor.builder()
                    .name("name")
                    .description("description")
                    .hospital(hospital)
                    .build();
            ReflectionTestUtils.setField(doctor, "id", 1L);


            Patient patient = Patient.builder()
                    .name("patientName")
                    .address("address")
                    .phone("111-1111")
                    .gender(Gender.M)
                    .build();
            ReflectionTestUtils.setField(patient, "id", 1L);


            User user = new User("test@email.com", "password", UserRole.ROLE_PATIENT, doctor, patient);
            ReflectionTestUtils.setField(user, "id", 1L);


            MedicalRecord medicalRecord = new MedicalRecord(
                    "Description",
                    LocalDateTime.now(),
                    patient,
                    doctor,
                    "Test Plan",
                    "Doctor Comment"
            );
            ReflectionTestUtils.setField(medicalRecord, "medicalRecordId", 1L);

            ReviewResponseDto responseDto = new ReviewResponseDto(
                    updateRequestDto.getMedicalRecordId(),
                    authUser.getId(),
                    updateRequestDto.getStar(),
                    updateRequestDto.getContents(),
                    "Patient A"
            );

            Mockito.when(reviewService.updateReview(reviewId, updateRequestDto, authUser)).thenReturn(responseDto);

            // MockMvc를 사용하여 PUT 요청 수행 및 검증
            mockMvc.perform(put("/reviews/{reviewId}", reviewId)
                            .with(authentication(authToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jacksonObjectMapper.writeValueAsString(updateRequestDto))
                            .with(csrf()))
                    .andExpect(status().isOk());

        }
    }

    @Nested
    public class 리뷰_삭제_컨트롤러 {

        @Test
        @DisplayName("리뷰 삭제 - 성공")
        public void deleteReview_Success() throws Exception {
            Long reviewId = 1L;

            // 테스트를 위한 AuthUser 설정
            AuthUser authUser = new AuthUser(1L, "testuser@gmail.com", UserRole.ROLE_PATIENT, null, null, null);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authUser);

            // Mock 설정
            Mockito.doNothing().when(reviewService).deleteReview(reviewId, authUser);

            // MockMvc를 사용하여 DELETE 요청 수행 및 검증
            mockMvc.perform(delete("/reviews/{reviewId}", reviewId)
                            .with(authentication(authToken))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Ok"))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data").doesNotExist());  // data 필드는 null이므로 존재하지 않음을 확인
        }
    }
}
