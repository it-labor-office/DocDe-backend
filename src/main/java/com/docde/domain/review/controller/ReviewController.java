package com.docde.domain.review.controller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.review.dto.request.ReviewRequestDto;
import com.docde.domain.review.dto.request.ReviewUpdateRequestDto;
import com.docde.domain.review.dto.response.ReviewResponseDto;
import com.docde.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ApiResponse<ReviewResponseDto> createReview(@AuthenticationPrincipal AuthUser authUser,
                                                       @RequestBody ReviewRequestDto reviewRequestDto) {

        ReviewResponseDto responseDto = reviewService.createReview(authUser, reviewRequestDto);

        return ApiResponse.createSuccess("리뷰 작성이 성공적으로 생성되었습니다.", 200, responseDto);

    }


    // 모든 리뷰 조회
    @GetMapping("/reviews")
    public ApiResponse<List<ReviewResponseDto>> getAllReviews() {

        List<ReviewResponseDto> reviews = reviewService.getAllReviews();
        return ApiResponse.onSuccess(reviews);
    }


    // 특정 사용자 리뷰 조회
    @GetMapping("/reviews/user/{userId}")
    public ApiResponse<List<ReviewResponseDto>> getReviewsByUserId(@PathVariable Long userId) {

        List<ReviewResponseDto> reviews = reviewService.getReviewsByUserId(userId);
        return ApiResponse.onSuccess(reviews);
    }


    // 리뷰 수정
    @PutMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser) {

        ReviewResponseDto responseDto = reviewService.updateReview(reviewId, requestDto, authUser);
        return ApiResponse.onSuccess(responseDto);
    }


    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal AuthUser authUser) {

        reviewService.deleteReview(reviewId, authUser);
        return ApiResponse.onSuccess(null);
    }
}