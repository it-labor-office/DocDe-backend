package com.docde.domain.medicalRecord.controller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.medicalRecord.dto.request.DoctorMedicalRecordRequestDto;
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
            @AuthenticationPrincipal AuthUser authUser) {

        // 진료 기록 생성
        MedicalRecordResponseDto responseDto = medicalRecordService.createMedicalRecord(requestDto, authUser);

        return ApiResponse.createSuccess("진료 기록이 성공적으로 생성되었습니다.", 200, responseDto);
    }

    // 특정 진료기록 조회
    @GetMapping("/doctors/medical-records/{medicalRecordId}")
    public ApiResponse<DoctorMedicalRecordResponseDto> getSpecificDoctorMedicalRecord(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long medicalRecordId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String treatmentPlan,
            @RequestParam(required = false) String doctorComment) {

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
            @AuthenticationPrincipal AuthUser authUser) {

        MedicalRecordResponseDto responseDto = medicalRecordService.updateMedicalRecord(
                medicalRecordId,
                doctorRequestDto,
                authUser
        );

        return ApiResponse.onSuccess(responseDto);
    }


/*    // 의사용 진료기록 수정
    @PutMapping("/doctors/medical-records/{medicalRecordId}")
    public ApiResponse<DoctorMedicalRecordResponseDto> updateDoctorMedicalRecord(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long medicalRecordId, @RequestBody DoctorMedicalRecordRequestDto requestDto) {

        DoctorMedicalRecordResponseDto responseDto = medicalRecordService.updateDoctorMedicalRecord(
                authUser,
                medicalRecordId,
                requestDto);

        return ApiResponse.onSuccess(responseDto);
    }


    // 환자용 진료기록 수정
    @PutMapping("/patients/medical-records/{medicalRecordId}")
    public ApiResponse<PatientMedicalRecordResponseDto> updatePatientMedicalRecord(
            @PathVariable Long medicalRecordId,
            @RequestBody PatientMedicalRecordRequestDto requestDto,
            @AuthenticationPrincipal AuthUser authUser) {

        PatientMedicalRecordResponseDto responseDto = medicalRecordService.updatePatientMedicalRecord(
                medicalRecordId,
                requestDto,
                authUser
        );

        return ApiResponse.onSuccess(responseDto);
    }*/

    // 진료기록 삭제
    @DeleteMapping("/medical-records/{medicalRecordId}")
    public ApiResponse<Void> deleteMedicalRecord(
            @PathVariable Long medicalRecordId,
            @AuthenticationPrincipal AuthUser authUser) {

        medicalRecordService.deleteMedicalRecord(medicalRecordId, authUser);
        return ApiResponse.onSuccess(null);

    }
}