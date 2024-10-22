package com.docde.domain.hospital.dto.request;

import com.docde.domain.hospital.entity.DayOfTheWeek;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class HospitalTimetablePostRequestDto {
    private DayOfTheWeek dayOfTheWeek;
    private LocalTime openTime;
    private LocalTime closingTime;
}
