package com.docde.domain.review.dto.response;

import lombok.Getter;

@Getter
public class ReviewResponseDto {

    private final Long medicalRecordId;
    private final Long userId;
    private final Long star;
    private final String contents;
    private final String patientName;

    public ReviewResponseDto(Long medicalRecordId, Long userId, Long star, String contents, String patientName) {
        this.medicalRecordId = medicalRecordId;
        this.userId = userId;
        this.star = star;
        this.contents = contents;
        this.patientName = patientName;
    }

}
