package com.docde.domain.hospital.dto.request;

import com.docde.domain.hospital.entity.Hospital;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalPostRequestDto {
    private String hospitalName;

    private String hospitalAddress;

    private String hospitalContact;
    @JsonFormat(pattern = "HH:mm") //시간 형식 지정
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm") //시간 형식 지정
    private LocalTime closingTime;

    private String announcement;


}
