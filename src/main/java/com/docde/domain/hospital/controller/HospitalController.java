package com.docde.domain.hospital.controller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.common.enums.UserRole;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.hospital.dto.request.*;
import com.docde.domain.hospital.dto.response.*;
import com.docde.domain.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hospitals")
public class HospitalController {
    private final HospitalService hospitalService;

//    @GetMapping //테스트용 코드
//    public ApiResponse<Null> test(@AuthenticationPrincipal UserDetails userDetails) {
//        hospitalService.ModifyingTest();
//        return ApiResponse.onSuccess(null);
//    }

    //병원 정보 생성
    @PostMapping
    //권한체크를 컨트롤러부분에서할수있습니다.
    @Secured(UserRole.Authority.DOCTOR_PRESIDENT)
    public ResponseEntity<ApiResponse<HospitalPostResponseDto>> postHospital(@RequestBody HospitalPostRequestDto requestDto,
                                                                             @AuthenticationPrincipal AuthUser authUser) {
        HospitalPostResponseDto responseDto = hospitalService.postHospital(requestDto, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onCreated(responseDto));
    }

    @PostMapping("/{hospitalId}")
    //추가하려는 병원과 유저의 소속병원은 같아야함.
    @PreAuthorize("#authUser.hospitalId == #hospitalId and hasRole('ROLE_DOCTOR_PRESIDENT')")
    public ResponseEntity<ApiResponse<HospitalPostDoctorResponseDto>> addDoctorToHospital(
            @PathVariable Long hospitalId,
            @RequestBody HospitalPostDoctorRequestDto requestDto
            , @AuthenticationPrincipal AuthUser authUser) {
        HospitalPostDoctorResponseDto responseDto = hospitalService.addDoctorToHospital(hospitalId, requestDto, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onCreated(responseDto));
    }

    //병원 정보 읽어 오기
    @GetMapping("/{hospitalId}")
    public ApiResponse<HospitalGetResponseDto> getHospital(@PathVariable Long hospitalId,
                                                           @AuthenticationPrincipal AuthUser authUser) {
        HospitalGetResponseDto responseDto = hospitalService.getHospital(hospitalId, authUser);
        return ApiResponse.onCreated(responseDto);
    }

    //병원 정보 수정
    @PutMapping("/{hospitalId}")
    @Secured(UserRole.Authority.DOCTOR_PRESIDENT)
    @PreAuthorize("#authUser.hospitalId == #hospitalId and hasRole('ROLE_DOCTOR_PRESIDENT')")
    public ApiResponse<HospitalUpdateResponseDto> putHospitalInfo(@RequestBody HospitalUpdateRequestDto requestDto,
                                                                  @PathVariable Long hospitalId,
                                                                  @AuthenticationPrincipal AuthUser authUser) {
        HospitalUpdateResponseDto responseDto = hospitalService.putHospital(requestDto, hospitalId, authUser);
        return ApiResponse.onSuccess(responseDto);
    }


    @PatchMapping("/{hospitalId}")
    @Secured(UserRole.Authority.DOCTOR_PRESIDENT)
    @PreAuthorize("#authUser.hospitalId == #hospitalId and hasRole('ROLE_DOCTOR_PRESIDENT')")
    public ApiResponse<HospitalUpdateResponseDto> patchHospitalInfo(@RequestBody HospitalUpdateRequestDto requestDto,
                                                                    @PathVariable Long hospitalId,
                                                                    @AuthenticationPrincipal AuthUser authUser) {
        HospitalUpdateResponseDto responseDto = hospitalService.patchHospital(requestDto, hospitalId, authUser);
        return ApiResponse.onSuccess(responseDto);
    }

    //병원 시간표 생성
    @PostMapping("/{hospitalId}/time-table")
    //본인 병원의 병원장만 생성,수정 가능
    @PreAuthorize("#hospitalId==#authUser.hospitalId and hasRole('ROLE_DOCTOR_PRESIDENT')")
    public ResponseEntity<ApiResponse<HospitalWeeklyTimetablePostResponseDto>> postWeeklyTimetables(
            @PathVariable Long hospitalId,
            @RequestBody HospitalWeeklyTimetablePostRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser) {
        HospitalWeeklyTimetablePostResponseDto responseDto = hospitalService.postWeeklyTimetable(
                requestDto,
                authUser,
                hospitalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onCreated(responseDto));
    }

    //병원 시간표 수정
    @PatchMapping("/{hospitalId}/time-table")
    @PreAuthorize("#hospitalId==#authUser.hospitalId and hasRole('ROLE_DOCTOR_PRESIDENT')")
    public ResponseEntity<ApiResponse<HospitalWeeklyTimetableUpdateResponseDto>> patchWeeklyTimetables(
            @PathVariable Long hospitalId,
            @RequestBody HospitalWeeklyTimetableUpdateRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser) {
        HospitalWeeklyTimetableUpdateResponseDto responseDto = hospitalService.updateWeeklyTimetable(
                requestDto,
                authUser,
                hospitalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onCreated(responseDto));
    }

    @DeleteMapping("/{hospitalId}")
    @PreAuthorize("#hospitalId==#authUser.hospitalId and hasRole('ROLE_DOCTOR_PRESIDENT')")
    public ApiResponse<HospitalDeleteResponseDto> deleteHospital(@AuthenticationPrincipal AuthUser authUser,
                                                                 @PathVariable Long hospitalId) {
        HospitalDeleteResponseDto responseDto = hospitalService.deleteHospital(authUser);
        return ApiResponse.onSuccess(responseDto);
    }
}
