package com.docde.domain.hospital.dto.request;

import com.docde.domain.hospital.entity.DayOfTheWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
@Getter
@NoArgsConstructor
public class HospitalWeeklyTimetablePostRequestDto {
    private List<TimetableDto> timetables;

    @Data
    public static class TimetableDto {
        private DayOfTheWeek dayOfTheWeek;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime openTime;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime closingTime;
    }
}
