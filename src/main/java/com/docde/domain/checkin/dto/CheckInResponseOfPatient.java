package com.docde.domain.checkin.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CheckInResponseOfPatient {
    private final String patientName;
    private final String doctorName;
    private final LocalDateTime checkedInAt;
    private final Long queue;

    public CheckInResponseOfPatient(String patientName, String doctorName, LocalDateTime checkedInAt, Long queue) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.checkedInAt = checkedInAt;
        this.queue = queue;
    }
}
