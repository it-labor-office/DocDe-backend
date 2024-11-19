package com.docde.domain.medicalRecord.controller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.medicalRecord.dto.request.DoctorMedicalRecordRequestDto;
import com.docde.domain.medicalRecord.dto.response.DoctorMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.MedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.PatientMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/medical-records")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDto>> createMedicalRecord(
            @RequestBody DoctorMedicalRecordRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser) throws Exception {

        // 진료 기록 생성
        MedicalRecordResponseDto responseDto = medicalRecordService.createMedicalRecord(requestDto, authUser);

        ApiResponse<MedicalRecordResponseDto> apiResponse =
                ApiResponse.createSuccess("진료 기록이 성공적으로 생성되었습니다.", 201, responseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }


    // 특정 진료기록 조회
    @GetMapping("/doctors/medical-records/{medicalRecordId}")
    public ApiResponse<DoctorMedicalRecordResponseDto> getSpecificDoctorMedicalRecord(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long medicalRecordId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String treatmentPlan,
            @RequestParam(required = false) String doctorComment) throws Exception {

        DoctorMedicalRecordResponseDto responseDto = medicalRecordService
                .getSpecificDoctorMedicalRecord(authUser, medicalRecordId, description, treatmentPlan, doctorComment);
        return ApiResponse.onSuccess(responseDto);
    }


    // 의사가 의사용 진료기록 조회
    @GetMapping("/doctors/medical-records")
    public ApiResponse<List<DoctorMedicalRecordResponseDto>> getDoctorMedicalRecord(
            @AuthenticationPrincipal AuthUser authUser) {

        List<DoctorMedicalRecordResponseDto> responseDto = medicalRecordService.getDoctorMedicalRecord(authUser);
        return ApiResponse.onSuccess(responseDto);
    }


    // 환자가 자신의 진료기록 조회
    @GetMapping("/patients/medical-records")
    public ApiResponse<List<PatientMedicalRecordResponseDto>> getPatientMedicalRecord(
            @AuthenticationPrincipal AuthUser authUser) {

        List<PatientMedicalRecordResponseDto> records = medicalRecordService.getPatientMedicalRecord(authUser);
        return ApiResponse.onSuccess(records);
    }


    // 진료기록 수정
    @PutMapping("/doctors/medical-records/{medicalRecordId}")
    public ApiResponse<MedicalRecordResponseDto> updateMedicalRecord(
            @PathVariable Long medicalRecordId,
            @RequestBody DoctorMedicalRecordRequestDto doctorRequestDto,
            @AuthenticationPrincipal AuthUser authUser) throws Exception {

        MedicalRecordResponseDto responseDto = medicalRecordService.updateMedicalRecord(
                medicalRecordId,
                doctorRequestDto,
                authUser
        );

        return ApiResponse.onSuccess(responseDto);
    }


    // 진료기록 삭제
    @DeleteMapping("/medical-records/{medicalRecordId}")
    public ApiResponse<Void> deleteMedicalRecord(
            @PathVariable Long medicalRecordId,
            @AuthenticationPrincipal AuthUser authUser) {

        medicalRecordService.deleteMedicalRecord(medicalRecordId, authUser);
        return ApiResponse.onSuccess(null);

    }
}