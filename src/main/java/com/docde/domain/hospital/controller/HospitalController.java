package com.docde.domain.hospital.controller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.hospital.dto.request.HospitalPatchRequestDto;
import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalPutRequestDto;
import com.docde.domain.hospital.dto.request.HospitalWeeklyTimetablePostRequestDto;
import com.docde.domain.hospital.dto.response.*;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hospitals")
public class HospitalController {
    private final HospitalService hospitalService;
    //병원 정보 생성
    @PostMapping
    public ApiResponse<HospitalPostResponseDto> postHospital(@RequestBody HospitalPostRequestDto requestDto,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        HospitalPostResponseDto responseDto = hospitalService.postHospital(requestDto,userDetails);
        return ApiResponse.onCreated(responseDto);
    }
    @GetMapping("/{hospitalId}")
    public ApiResponse<HospitalGetResponseDto> getHospital(@PathVariable Long hospitalId,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        HospitalGetResponseDto responseDto = hospitalService.getHospital(hospitalId,userDetails);
        return ApiResponse.onCreated(responseDto);
    }

    //병원 정보 수정
    @PutMapping
    public ApiResponse<HospitalPutResponseDto> putHospital(@RequestBody HospitalPutRequestDto requestDto,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        HospitalPutResponseDto responseDto = hospitalService.putHospital(requestDto,userDetails);
        return ApiResponse.onSuccess(responseDto);
    }

    //병원 시간표 생성
    @PostMapping("/{hospitalId}/time-table")
    public ApiResponse<HospitalWeeklyTimetablePostResponseDto> postWeeklyTimetables(
            @PathVariable Long hospitalId,
            @RequestBody HospitalWeeklyTimetablePostRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        HospitalWeeklyTimetablePostResponseDto responseDto = hospitalService.postWeeklyTimetable(
                requestDto,
                userDetails,
                hospitalId);
        return ApiResponse.onCreated(responseDto);
    }

    @DeleteMapping
    public ApiResponse<HospitalDeleteResponseDto> deleteHospital(
            @RequestBody HospitalDeleteRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        HospitalDeleteResponseDto responseDto = hospitalService.deleteHospital(requestDto,userDetails);
        return ApiResponse.onSuccess(responseDto);
    }
}
