package com.docde.domain.hospital.dto.request;

import com.docde.domain.hospital.dto.TimetableDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class HospitalWeeklyTimetablePostRequestDto {
    private List<TimetableDto> timetables;

    public HospitalWeeklyTimetablePostRequestDto(List<TimetableDto> timetables) {
        this.timetables = timetables;
    }
}
