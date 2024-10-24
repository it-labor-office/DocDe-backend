package com.docde.domain.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class HospitalPutRequestDto {
    private Long hospitalId;

    private String hospitalName;

    private String hospitalAddress;

    private String hospitalContact;
    @JsonFormat(pattern = "HH:mm") //시간 형식 지정
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm") //시간 형식 지정
    private LocalTime closingTime;

    private String announcement;
}
