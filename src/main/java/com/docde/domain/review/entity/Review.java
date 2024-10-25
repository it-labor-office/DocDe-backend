package com.docde.domain.review.entity;

import com.docde.common.entity.Timestamped;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
@NoArgsConstructor
@Getter
public class Review extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Min(value = 1, message = "별점은 최소 1 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 최대 5 이하이어야 합니다.")
    private Long star;

    @NotBlank(message = "내용은 비어있을 수 없습니다.")

    private String contents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Review(Long star, String contents, User user, MedicalRecord medicalRecord) {
        this.star = star;
        this.contents = contents;
        this.user = user;
        this.medicalRecord = medicalRecord;
    }

    public Review(Long reviewId, Long star, String contents, User user, MedicalRecord medicalRecord) {

    this.reviewId = reviewId;
    this.star = star;
    this.contents = contents;
    this.user = user;
    this.medicalRecord = medicalRecord;
    }
}
