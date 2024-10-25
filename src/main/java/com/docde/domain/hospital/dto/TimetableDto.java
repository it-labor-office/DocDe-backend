package com.docde.domain.hospital.dto;

import com.docde.domain.hospital.entity.DayOfTheWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class TimetableDto {
    private DayOfTheWeek dayOfTheWeek;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    public TimetableDto(DayOfTheWeek dayOfTheWeek, LocalTime openTime, LocalTime closingTime) {
        this.dayOfTheWeek = dayOfTheWeek;
        this.openTime = openTime;
        this.closingTime = closingTime;
    }
}
