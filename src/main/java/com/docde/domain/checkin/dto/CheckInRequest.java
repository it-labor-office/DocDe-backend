package com.docde.domain.checkin.dto;

import lombok.Getter;

@Getter
public class CheckInRequest {
    private Long doctorId;
    private String status;
}
