package com.docde.domain.hospital.dto.response;

import com.docde.domain.hospital.entity.Hospital;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class HospitalUpdateResponseDto {
    private String hospitalName;

    private String hospitalAddress;

    private String hospitalContact;

    private LocalTime openTime;

    private LocalTime closingTime;

    private String announcement;

    public HospitalUpdateResponseDto(Hospital hospital) {
        this.hospitalName = hospital.getName();
        this.hospitalAddress = hospital.getAddress();
        this.hospitalContact = hospital.getContact();
        this.openTime = hospital.getOpenTime();
        this.closingTime = hospital.getClosingTime();
        this.announcement = hospital.getAnnouncement();
    }
}
