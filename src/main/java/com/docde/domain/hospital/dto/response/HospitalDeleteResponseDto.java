package com.docde.domain.hospital.dto.response;

import com.docde.domain.hospital.entity.Hospital;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HospitalDeleteResponseDto {
    private Long Id;

    public HospitalDeleteResponseDto(Hospital hospital) {
        this.Id = hospital.getId();
    }
}
