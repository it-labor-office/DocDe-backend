package com.docde.domain.checkin.dto;

import com.docde.domain.checkin.entity.CheckIn;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CheckInResponse {
    private final Long id;
    private final String patientName;
    private final String doctorName;
    private final LocalDateTime checkedInAt;


    public CheckInResponse(Long id, String patientName, String doctorName, LocalDateTime checkedInAt) {
        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.checkedInAt = checkedInAt;
    }
}
