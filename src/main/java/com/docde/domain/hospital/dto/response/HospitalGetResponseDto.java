package com.docde.domain.hospital.dto.response;

import com.docde.domain.hospital.entity.Hospital;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class HospitalGetResponseDto {
    private Long hospitalId;

    private String hospitalName;

    private String hospitalAddress;

    private String hospitalContact;

    private LocalTime openTime;

    private LocalTime closingTime;

    private String announcement;

//    private List<HospitalDoctorsDto> doctorsDtoList;
//
//    @Data
//    public static class HospitalDoctorsDto {
//        private Long DoctorId;
//        private String DoctorName;
//        private String DoctorAnnouncement;
//    }

    public HospitalGetResponseDto(Hospital hospital) {
        this.hospitalId = hospital.getId();
        this.hospitalName = hospital.getName();
        this.hospitalAddress = hospital.getAddress();
        this.hospitalContact = hospital.getContact();
        this.openTime = hospital.getOpen_time();
        this.closingTime = hospital.getClosing_time();
        this.announcement = hospital.getAnnouncement();
    }

}