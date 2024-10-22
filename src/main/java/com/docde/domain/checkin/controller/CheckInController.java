package com.docde.domain.checkin.controller;

import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CheckInResponse> saveCheckIn(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long hospitalId,
            @RequestBody CheckInRequest checkInRequest
            ){
        return ResponseEntity.ok(checkInService.saveCheckIn(userDetails, hospitalId, checkInRequest));
    }

    // 자신의 접수 상태 확인(사용자)
    @GetMapping("/checkin")
    public ResponseEntity<CheckInResponse> getMyCheckIn(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(checkInService.getMyCheckIn(userDetails));
    }

    // 접수 상태 확인(병원)
    @GetMapping("{hospitalId}/checkin/all")
    public ResponseEntity<List<CheckInResponse>> getAllCheckIns(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long hospitalId
    ){
        return ResponseEntity.ok(checkInService.getAllCheckIns(userDetails, hospitalId));
    }

    // 접수 상태 변경
    // 접수 기록 영구 삭제

}
