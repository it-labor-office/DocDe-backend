package com.docde.domain.review.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.medicalRecord.repository.MedicalRecordRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.review.dto.request.ReviewRequestDto;
import com.docde.domain.review.dto.request.ReviewUpdateRequestDto;
import com.docde.domain.review.dto.response.ReviewResponseDto;
import com.docde.domain.review.entity.Review;
import com.docde.domain.review.repository.ReviewRepository;
import com.docde.domain.user.entity.User;
import com.docde.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.docde.domain.patient.entity.QPatient.patient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    private User user;
    private MedicalRecord medicalRecord;
    private Review review;
    private AuthUser authUser;


    @BeforeEach
    void setUp() {  // 테스트의 준비 작업 수행 역할

        // Mock User 생성
                user = User.builder()
                .email("test@example.com")
                .password("password")
                .userRole(UserRole.ROLE_PATIENT)
                .doctor(null)
                .patient(null)
                .build();

        try {
            Field idField = user.getClass().getDeclaredField("id");
            idField.setAccessible(true); // private 접근 허용
            idField.set(user, 1L);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        authUser = AuthUser.builder()
                .id(1L)
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .doctorId(user.getDoctor() != null ? user.getDoctor().getId() : null)
                .patientId(user.getPatient() != null ? user.getPatient().getId() : null)
                .hospitalId(null)
                .build();

        Patient patient = new Patient();
        ReflectionTestUtils.setField(patient,"name","test");

        medicalRecord = new MedicalRecord();
        ReflectionTestUtils.setField(medicalRecord,"patient",patient);
        ReflectionTestUtils.setField(medicalRecord,"medicalRecordId",1L);

        review = new Review(1L, 5L, "contents", user, medicalRecord);
    }

    @Test
    public void 리뷰_생성_성공() {

        // given
        ReviewRequestDto requestDto = new ReviewRequestDto(
                medicalRecord.getMedicalRecordId(),
                user.getId(),
                5L,
                "jmt"
        );
        Review savedReview = new Review(5L, "jmt", user, medicalRecord);
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(medicalRecordRepository.findById(medicalRecord.getMedicalRecordId())).willReturn(Optional.of(medicalRecord));

        // when
        ReviewResponseDto responseDto = reviewService.createReview(authUser, requestDto);

        // then
        assertEquals(medicalRecord.getMedicalRecordId(), responseDto.getMedicalRecordId());
        assertEquals(user.getId(), responseDto.getUserId());
        assertEquals(5L, responseDto.getStar());
        assertEquals("jmt", responseDto.getContents());

    }


    @Test
    public void 리뷰_생성_시_유저를_찾을_수_없음() {

        ReviewRequestDto requestDto = new ReviewRequestDto(medicalRecord.getMedicalRecordId(),user.getId(), 5L, "jmt");

        // given
        given(userRepository.findById(authUser.getId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class,
                () -> reviewService.createReview(authUser, requestDto));

        assertEquals(ErrorStatus._BAD_REQUEST_NOT_FOUND_USER, exception.getErrorCode());
    }


    @Test
    void 리뷰_생성_시_진료기록_찾을_수_없음() {

        ReviewRequestDto requestDto = new ReviewRequestDto(medicalRecord.getMedicalRecordId(),user.getId(), 5L, "jmt");

        // given
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(medicalRecordRepository.findById(requestDto.getMedicalRecordId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class,
                () -> reviewService.createReview(authUser, requestDto));

        assertEquals(ErrorStatus._NOT_FOUND_MEDICAL_RECORD, exception.getErrorCode());
    }


    @Test
    public void 특정_사용자에_대한_리뷰_조회_성공() {
        // given
        Long userId = 1L; // 테스트할 사용자 ID
        List<Review> reviews = Arrays.asList(
                new Review(1L, 5L, "굿", user, medicalRecord),
                new Review(2L, 4L, "낫베드", user, medicalRecord)
        );

        // Mocking
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewRepository.findByUser(user)).willReturn(reviews);

        // when
        List<ReviewResponseDto> responseDto = reviewService.getReviewsByUserId(userId);

        // then
        assertEquals(2, responseDto.size());
        assertEquals("굿", responseDto.get(0).getContents());
        assertEquals(5L, responseDto.get(0).getStar());
        assertEquals("test", responseDto.get(0).getPatientName());
    }


    @Test
    void 특정_사용자에_대한_리뷰_조회_시_사용자를_찾을_수_없음() {
        // 주어진
        Long userId = 1L;

        // 사용자를 찾지 못하는 경우를 설정
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class,
                () -> reviewService.getReviewsByUserId(userId));

        assertEquals(ErrorStatus._BAD_REQUEST_NOT_FOUND_USER, exception.getErrorCode());
    }


    @Test
    void 특정_사용자에_대한_리뷰_조회_시_리뷰를_찾을_수_없음() {

        Long userId = 1L;

        //given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewRepository.findByUser(user)).willReturn(Collections.emptyList());

        // when & then
        ApiException exception = assertThrows(ApiException.class,
                () -> reviewService.getReviewsByUserId(userId));

        assertEquals(ErrorStatus._NOT_FOUND_REVIEW, exception.getErrorCode());
    }


    @Test
    void 모든_리뷰_조회_성공() {

        Patient patient2 = new Patient();
        ReflectionTestUtils.setField(patient2, "name", "Patient");

        MedicalRecord medicalRecord2 = new MedicalRecord();
        ReflectionTestUtils.setField(medicalRecord2, "patient", patient2);
        ReflectionTestUtils.setField(medicalRecord2,"medicalRecordId",2L);


        Review review2 = new Review(2L, 4L, "contents2", user, medicalRecord2);

        given(reviewRepository.findAll()).willReturn(List.of(review, review2));
        System.out.println(review.getMedicalRecord().getMedicalRecordId());

        // when
        List<ReviewResponseDto> response = reviewService.getAllReviews();

        // then
        assertEquals(2, response.size());

        // 첫번째 리뷰 검증
        assertEquals(1L, response.get(0).getMedicalRecordId());
        assertEquals(5L, response.get(0).getStar());
        assertEquals("contents", response.get(0).getContents());

        // 두번째 리뷰 검증
        assertEquals(2L, response.get(1).getMedicalRecordId());
        assertEquals(4L, response.get(1).getStar());
        assertEquals("contents2", response.get(1).getContents());
    }


    @Test
    public void 리뷰_업데이트_성공() {
        // given
        Long reviewId = review.getReviewId();
        ReviewUpdateRequestDto requestDto = new ReviewUpdateRequestDto(
                4L, // 새로운 별점
                "업데이트된 Content", // 새로운 내용
                medicalRecord.getMedicalRecordId() // MedicalRecord ID
        );

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(medicalRecordRepository.findById(requestDto.getMedicalRecordId())).willReturn(Optional.of(medicalRecord));

        given(reviewRepository.save(any(Review.class))).willReturn(new Review(
                review.getReviewId(),
                requestDto.getStar(),
                requestDto.getContents(),
                user,
                medicalRecord
        ));

        // when
        ReviewResponseDto responseDto = reviewService.updateReview(reviewId, requestDto, authUser);

        // then
        assertEquals(medicalRecord.getMedicalRecordId(), responseDto.getMedicalRecordId());
        assertEquals(user.getId(), responseDto.getUserId());
        assertEquals(4L, responseDto.getStar());
        assertEquals("업데이트된 Content", responseDto.getContents());
        assertEquals(medicalRecord.getPatient().getName(), responseDto.getPatientName());

        //
        verify(reviewRepository).save(any(Review.class));
    }


    @Test
    public void 리뷰_수정_실패_리뷰_존재하지_않음() {
        // given
        Long reviewId = 999L; // 존재하지 않는 리뷰 ID
        ReviewUpdateRequestDto requestDto = new ReviewUpdateRequestDto(4L, "업데이트된 Content", 1L);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            reviewService.updateReview(reviewId, requestDto, authUser);
        });

        assertEquals(ErrorStatus._NOT_FOUND_REVIEW, exception.getErrorCode());
    }


    @Test
    public void 리뷰_수정_실패_사용자_존재하지_않음() {
        // given
        Long reviewId = review.getReviewId();
        ReviewUpdateRequestDto requestDto = new ReviewUpdateRequestDto(4L, "업데이트된 Content", 1L);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            reviewService.updateReview(reviewId, requestDto, authUser);
        });

        assertEquals(ErrorStatus._BAD_REQUEST_NOT_FOUND_USER, exception.getErrorCode());
    }


    @Test
    public void 리뷰_삭제_성공() {
        // given
        Long reviewId = review.getReviewId();
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));

        // when
        reviewService.deleteReview(reviewId, authUser);

        // then
        verify(reviewRepository).deleteById(reviewId);
    }

    @Test
    public void 리뷰_삭제_실패_리뷰_존재하지_않음() {
        // given
        Long reviewId = 1111L; // 존재하지 않는 ID
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThrows(ApiException.class, () -> reviewService.deleteReview(reviewId, authUser));
    }
}

