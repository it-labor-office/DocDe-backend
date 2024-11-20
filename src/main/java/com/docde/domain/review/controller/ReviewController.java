package com.docde.domain.review.controller;

import com.docde.common.response.ApiResponse;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.review.dto.request.ReviewRequestDto;
import com.docde.domain.review.dto.request.ReviewUpdateRequestDto;
import com.docde.domain.review.dto.response.ReviewResponseDto;
import com.docde.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody ReviewRequestDto reviewRequestDto) {

        ReviewResponseDto responseDto = reviewService.createReview(authUser, reviewRequestDto);
        return ApiResponse.onCreated(responseDto).toEntity();
    }


    // 모든 리뷰 조회
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getAllReviews() {

        List<ReviewResponseDto> reviews = reviewService.getAllReviews();
        return ApiResponse.onSuccess(reviews).toEntity();
    }


    // 특정 사용자 리뷰 조회
    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getReviewsByUserId(@PathVariable Long userId) {

        List<ReviewResponseDto> reviews = reviewService.getReviewsByUserId(userId);
        return ApiResponse.onSuccess(reviews).toEntity();
    }


    // 리뷰 수정
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser) {

        ReviewResponseDto responseDto = reviewService.updateReview(reviewId, requestDto, authUser);
        return ApiResponse.onSuccess(responseDto).toEntity();
    }


    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser authUser) {

        reviewService.deleteReview(reviewId, authUser);
        return ResponseEntity.noContent().build();
    }
}