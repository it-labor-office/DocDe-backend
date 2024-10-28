package com.docde.domain.hospital.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HospitalPostDoctorResponseDto {
    private Long doctorId;
    private String doctorName;
    private Long hospitalId;
    private String hospitalName;

    public HospitalPostDoctorResponseDto(Long doctorId, String doctorName, Long hospitalId, String hospitalName) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
    }
}
