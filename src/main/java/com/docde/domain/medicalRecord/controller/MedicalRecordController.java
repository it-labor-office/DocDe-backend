package com.docde.domain.medicalRecord.controller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.medicalRecord.dto.request.DoctorMedicalRecordRequestDto;
import com.docde.domain.medicalRecord.dto.request.PatientMedicalRecordRequestDto;
import com.docde.domain.medicalRecord.dto.response.DoctorMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.MedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.PatientMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;


    @PostMapping("/medical-records")
    public ApiResponse<MedicalRecordResponseDto> createMedicalRecord(
            @RequestBody DoctorMedicalRecordRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 진료 기록 생성
        MedicalRecordResponseDto responseDto = medicalRecordService.createMedicalRecord(requestDto, userDetails);

        return ApiResponse.createSuccess("진료 기록이 성공적으로 생성되었습니다.", 200, responseDto);
    }

    // 의사가 의사용 진료 기록 조회
    @GetMapping("/doctors")
    public ApiResponse<List<DoctorMedicalRecordResponseDto>> getDoctorMedicalRecord(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<DoctorMedicalRecordResponseDto> responseDto = medicalRecordService.getDoctorMedicalRecord(userDetails);
        return ApiResponse.onSuccess(responseDto);
    }


    // 환자가 자신의 진료 기록 조회
    @GetMapping("/patients")
    public ApiResponse<List<PatientMedicalRecordResponseDto>> getPatientMedicalRecord(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<PatientMedicalRecordResponseDto> records = medicalRecordService.getPatientMedicalRecord(userDetails);
        return ApiResponse.onSuccess(records);
    }


    // 의사용 진료기록 수정
    @PutMapping("doctors/medical-records/{medicalRecordId}")
    public ApiResponse<DoctorMedicalRecordResponseDto> updateDoctorMedicalRecord(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long medicalRecordId, @RequestBody DoctorMedicalRecordRequestDto requestDto) {

        DoctorMedicalRecordResponseDto responseDto = medicalRecordService.updateDoctorMedicalRecord(
                userDetails,
                medicalRecordId,
                requestDto);

        return ApiResponse.onSuccess(responseDto);
    }


    // 환자용 진료기록 수정
    @PutMapping("/patients/medical-records/{medicalRecordId}")
    public ApiResponse<PatientMedicalRecordResponseDto> updatePatientMedicalRecord(
            @PathVariable Long medicalRecordId,
            @RequestBody PatientMedicalRecordRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PatientMedicalRecordResponseDto responseDto = medicalRecordService.updatePatientMedicalRecord(
                medicalRecordId,
                requestDto,
                userDetails
        );

        return ApiResponse.onSuccess(responseDto);
    }


    // 진료기록 삭제
    @DeleteMapping("/medical-records/{medicalRecordId}")
    public ApiResponse<Void> deleteMedicalRecord(
            @PathVariable Long medicalRecordId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        medicalRecordService.deleteMedicalRecord(medicalRecordId, userDetails);
        return ApiResponse.onSuccess(null);

    }
}