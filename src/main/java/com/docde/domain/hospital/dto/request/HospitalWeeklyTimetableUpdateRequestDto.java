package com.docde.domain.hospital.dto.request;

import com.docde.domain.hospital.dto.TimetableDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class HospitalWeeklyTimetableUpdateRequestDto {
    private List<TimetableDto> timetables;

    public HospitalWeeklyTimetableUpdateRequestDto(List<TimetableDto> timetables) {
        this.timetables = timetables;
    }

    public void add(TimetableDto timetableDto) {
        this.timetables.add(timetableDto);
    }

    public void init() {
        this.timetables = new ArrayList<>();
    }
}
