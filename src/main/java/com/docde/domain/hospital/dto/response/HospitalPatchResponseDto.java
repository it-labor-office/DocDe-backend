package com.docde.domain.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalPatchResponseDto {
    private String hospitalName;

    private String hospitalAddress;

    private String hospitalContact;

    private LocalTime openTime;

    private LocalTime closingTime;

    private String announcement;
}
