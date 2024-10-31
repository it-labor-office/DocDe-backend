package com.docde.domain.review.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewRequestDto {

    private Long medicalRecordId;
    private Long userId;
    private Long star;
    private String contents;

}
