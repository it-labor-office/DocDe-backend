package com.docde.domain.hospital.controller;

import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.response.HospitalPostResponseDto;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/docde")
public class HospitalController {
    private final HospitalService hospitalService;
    //병원 정보 생성
    @PostMapping
    public ResponseEntity<HospitalPostResponseDto> postHospital(@RequestBody HospitalPostRequestDto requestDto) {
            HospitalPostResponseDto responseDto = hospitalService.postHospital(requestDto);
            return ResponseEntity.ok(responseDto);
    }
}
