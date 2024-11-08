package com.docde.domain.review.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto {

    private Long medicalRecordId;
    private Long userId;
    private Long star;
    private String contents;


    public ReviewRequestDto(Long medicalRecordId, Long userId, Long star, String contents) {
        this.medicalRecordId = medicalRecordId;
        this.userId = userId;
        this.star = star;
        this.contents = contents;

    }
}
