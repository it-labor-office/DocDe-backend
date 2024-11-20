package com.docde.domain.hospital.dto.response;


import com.docde.domain.hospital.dto.TimetableDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class HospitalWeeklyTimetablePostResponseDto {
    private List<TimetableDto> timetables;

    public HospitalWeeklyTimetablePostResponseDto(List<TimetableDto> timetables) {
        this.timetables = timetables;
    }
}
