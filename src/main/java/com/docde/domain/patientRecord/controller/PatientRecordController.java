package com.docde.domain.patientRecord.controller;

import com.docde.domain.patientRecord.dto.response.PatientRecordResponseDto;
import com.docde.domain.patientRecord.service.PatientRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PatientRecordController {

    private final PatientRecordService patientRecordsService;

    @PostMapping("/patientRecord")
    public ApiResponse<PatientRecordResponseDto>



}
