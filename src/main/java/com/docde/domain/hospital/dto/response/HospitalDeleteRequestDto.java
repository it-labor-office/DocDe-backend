package com.docde.domain.hospital.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HospitalDeleteRequestDto {
    private Long hospitalId;

    public HospitalDeleteRequestDto(Long hospitalId) {
        this.hospitalId = hospitalId;
    }
}
