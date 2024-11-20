package com.docde.domain.hospital.dto.response;

import com.docde.domain.hospital.dto.TimetableDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class HospitalWeeklyTimetableUpdateResponseDto {
    private List<TimetableDto> timetables;

    public HospitalWeeklyTimetableUpdateResponseDto(List<TimetableDto> timetables) {
        this.timetables = timetables;
    }
}
