package com.docde.domain.checkin.controller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/hospitals")
public class CheckInController {

    private final CheckInService checkInService;

    // 접수하기
    @PostMapping("/{hospitalId}/checkin")
    public ApiResponse<CheckInResponse> saveCheckIn(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long hospitalId,
            @RequestBody CheckInRequest checkInRequest
            ){
        return ApiResponse.onCreated(checkInService.saveCheckIn(userDetails, hospitalId, checkInRequest));
    }

    // 자신의 접수 상태 확인(사용자)
    @GetMapping("/checkin")
    public ApiResponse<CheckInResponse> getMyCheckIn(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ApiResponse.onSuccess(checkInService.getMyCheckIn(userDetails));
    }

    // 접수 상태 확인(병원)
    @GetMapping("{hospitalId}/checkin/all")
    public ApiResponse<List<CheckInResponse>> getAllCheckIns(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long hospitalId
    ){
        return ApiResponse.onSuccess(checkInService.getAllCheckIns(userDetails, hospitalId));
    }

    // 접수 상태 변경
    @PutMapping("/{hospitalId}/checkin/{checkInId}")
    public ApiResponse<CheckInResponse> updateCheckIn(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long hospitalId,
            @PathVariable Long checkInId,
            @RequestBody CheckInRequest checkInRequest
    ){
        return ApiResponse.onSuccess(checkInService.updateCheckIn(userDetails, hospitalId, checkInId, checkInRequest));
    }

    // 접수 기록 영구 삭제
    @DeleteMapping("/checkin/{checkInId}")
    public void deleteCheckIn(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long checkInId
    ){
        checkInService.deleteCheckIn(userDetails, checkInId);
    }

}
