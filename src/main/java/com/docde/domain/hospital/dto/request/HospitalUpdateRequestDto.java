package com.docde.domain.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class HospitalUpdateRequestDto {
    @NotNull
    private Long hospitalId;

    private String hospitalName;

    private String hospitalAddress;

    private String hospitalContact;
    @JsonFormat(pattern = "HH:mm") //시간 형식 지정
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm") //시간 형식 지정
    private LocalTime closingTime;

    private String announcement;

    public HospitalUpdateRequestDto(Long hospitalId, String hospitalName, String hospitalAddress, String hospitalContact, LocalTime openTime, LocalTime closingTime, String announcement) {
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.hospitalContact = hospitalContact;
        this.openTime = openTime;
        this.closingTime = closingTime;
        this.announcement = announcement;
    }
}