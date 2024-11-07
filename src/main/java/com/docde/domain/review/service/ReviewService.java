package com.docde.domain.review.service;


import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.medicalRecord.repository.MedicalRecordRepository;
import com.docde.domain.review.dto.request.ReviewRequestDto;
import com.docde.domain.review.dto.request.ReviewUpdateRequestDto;
import com.docde.domain.review.dto.response.ReviewResponseDto;
import com.docde.domain.review.entity.Review;
import com.docde.domain.review.repository.ReviewRepository;
import com.docde.domain.user.entity.User;
import com.docde.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepository;


    // 리뷰 생성
    @Transactional
    public ReviewResponseDto createReview(AuthUser authUser, ReviewRequestDto requestDto) {

        User user = userRepository.findById(authUser.getId()).orElseThrow(()
                -> new ApiException(ErrorStatus._BAD_REQUEST_NOT_FOUND_USER));

        MedicalRecord medicalRecord = medicalRecordRepository.findById(requestDto.getMedicalRecordId()).orElseThrow(()
                -> new ApiException(ErrorStatus._NOT_FOUND_MEDICAL_RECORD));


        Review review = new Review(
                requestDto.getStar(),
                requestDto.getContents(),
                user,
                medicalRecord
        );

        Review savedReview = reviewRepository.save(review);

        String patientName = medicalRecord.getPatient().getName();


        return new ReviewResponseDto(
                savedReview.getMedicalRecord().getMedicalRecordId(),
                savedReview.getUser().getId(),
                savedReview.getStar(),
                savedReview.getContents(),
                patientName

        );
    }


    // 모든 리뷰 조회
    public List<ReviewResponseDto> getAllReviews() {

        List<Review> reviews = reviewRepository.findAll();

        if (reviews.isEmpty()) {
            throw new ApiException(ErrorStatus._NOT_FOUND_REVIEW);
        }

        return reviews.stream()

                .map(review -> new ReviewResponseDto(
                        review.getMedicalRecord().getMedicalRecordId(),
                        review.getUser().getId(),
                        review.getStar(),
                        review.getContents(),
                        review.getMedicalRecord().getPatient().getName()
                ))
                .collect(Collectors.toList());
    }


    // 특정 사용자에 대한 리뷰 조회
    public List<ReviewResponseDto> getReviewsByUserId(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus._BAD_REQUEST_NOT_FOUND_USER));

        List<Review> reviews = reviewRepository.findByUser(user);

        if (reviews.isEmpty()) {
            throw new ApiException(ErrorStatus._NOT_FOUND_REVIEW);
        }

        return reviews.stream()
                .map(review -> new ReviewResponseDto(
                        review.getMedicalRecord().getMedicalRecordId(),
                        review.getUser().getId(),
                        review.getStar(),
                        review.getContents(),
                        review.getMedicalRecord().getPatient().getName()
                ))
                .collect(Collectors.toList());
    }


    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewUpdateRequestDto requestDto, AuthUser authUser) {

        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_REVIEW));

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._BAD_REQUEST_NOT_FOUND_USER));
        MedicalRecord medicalRecord = medicalRecordRepository.findById(requestDto.getMedicalRecordId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_MEDICAL_RECORD));

        // 업데이트할 필드만 설정하여 새로운 Review 객체 생성 (ID는 자동 관리)
        Review updatedReview = new Review(
                requestDto.getStar(),
                requestDto.getContents(),
                user,
                medicalRecord
        );

        Review savedReview = reviewRepository.save(updatedReview);

        return new ReviewResponseDto(
                savedReview.getMedicalRecord().getMedicalRecordId(),
                savedReview.getUser().getId(),
                savedReview.getStar(),
                savedReview.getContents(),
                savedReview.getMedicalRecord().getPatient().getName()
        );
    }


    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, AuthUser authUser) {

        Review review = reviewRepository.findById(reviewId).orElseThrow(()
                -> new ApiException(ErrorStatus._NOT_FOUND_REVIEW));

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._BAD_REQUEST_NOT_FOUND_USER));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ApiException(ErrorStatus._FORBIDDEN_ACCESS);
        }

        reviewRepository.deleteById(reviewId);
    }

}
