package com.docde.domain.hospital.dto.response;



import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class HospitalWeeklyTimetablePostResponseDto {
    private String message;

    public HospitalWeeklyTimetablePostResponseDto(String message) {
        this.message=message;
    }
}
