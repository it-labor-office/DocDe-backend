package com.docde.domain.hospital.dto.request;

import java.util.List;

public class HospitalWeeklyTimetablePostRequestDto {
    private Long hospitalId;
    private List<HospitalTimetablePostRequestDto> schedules;
}
