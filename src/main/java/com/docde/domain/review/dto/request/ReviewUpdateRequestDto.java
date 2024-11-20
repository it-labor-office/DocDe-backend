package com.docde.domain.review.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewUpdateRequestDto {

    private Long star;
    private String contents;
    private Long medicalRecordId;
}