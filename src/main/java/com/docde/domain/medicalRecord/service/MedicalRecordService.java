package com.docde.domain.medicalRecord.service;


import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.medicalRecord.dto.request.DoctorMedicalRecordRequestDto;
import com.docde.domain.medicalRecord.dto.request.PatientMedicalRecordRequestDto;
import com.docde.domain.medicalRecord.dto.response.DoctorMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.MedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.PatientMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.medicalRecord.repository.MedicalRecordRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;


    @Transactional
    public MedicalRecordResponseDto createMedicalRecord(DoctorMedicalRecordRequestDto requestDto, AuthUser authUser) {
        // 로그인한 의사 정보
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        // 환자 정보
        Patient patient = patientRepository.findById(requestDto.getPatientId()).orElseThrow(()
                -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));

        MedicalRecord medicalRecord = new MedicalRecord(
                requestDto.getDescription(),
                requestDto.getConsultation(),
                patient,
                doctor,
                requestDto.getTreatmentPlan(),
                requestDto.getDoctorComment()
        );

       /* // 병원 정보가 누락되지 않았는지 확인
        if (doctor.getHospital() == null) {
            throw new ApiException(ErrorStatus._NOT_FOUND_HOSPITAL);  // 병원 정보가 없을 경우 예외 처리
        }*/

        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);

        // 의사용 진료 기록
        DoctorMedicalRecordResponseDto doctorResponseDto = new DoctorMedicalRecordResponseDto(
                savedRecord.getMedicalRecordId(),
                savedRecord.getDescription(),
                savedRecord.getConsultation(),
                patient.getName(),
                patient.getId(),
                savedRecord.getTreatmentPlan(), // 치료 계획
                savedRecord.getDoctorComment()  // 의사 코멘트
        );

        // 환자용 진료 기록
        PatientMedicalRecordResponseDto patientResponseDto = new PatientMedicalRecordResponseDto(
                savedRecord.getMedicalRecordId(),
                savedRecord.getDescription(),
                savedRecord.getConsultation(),
                doctor.getName() // 의사 이름
        );

        return new MedicalRecordResponseDto(doctorResponseDto, patientResponseDto);
    }


    // 의사가 의사용 진료기록 조회
    public List<DoctorMedicalRecordResponseDto> getDoctorMedicalRecord(AuthUser authUser) {

        // 로그인한 의사 ID
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByDoctorId(doctor.getId()); // 의사 ID로 진료 기록 조회


        return medicalRecords.stream().map(record -> new DoctorMedicalRecordResponseDto(
                record.getMedicalRecordId(),
                record.getDescription(),
                record.getConsultation(),
                record.getPatient().getName(), // 환자 이름
                record.getPatient().getId(), // 환자 ID
                record.getTreatmentPlan(),
                record.getDoctorComment())).collect(Collectors.toList());
    }


    // 환자가 자신의 진료기록 조회
    public List<PatientMedicalRecordResponseDto> getPatientMedicalRecord(AuthUser authUser) {
        // 로그인한 환자 ID
        Patient patient = patientRepository.findByUser_Id(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));

        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPatientId(patient.getId()); // 환자 ID로 진료 기록 조회

        return medicalRecords.stream().map(record -> new PatientMedicalRecordResponseDto(record.getMedicalRecordId(),
                        record.getDescription(),
                        record.getConsultation(),
                        record.getDoctor().getName())) // 의사 이름
                .collect(Collectors.toList());
    }


    // 의사용 진료기록 수정
    @Transactional
    public DoctorMedicalRecordResponseDto updateDoctorMedicalRecord(AuthUser authUser,
                                                                    Long medicalRecordId,
                                                                    DoctorMedicalRecordRequestDto requestDto) {

        // 로그인 의사정보
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_MEDICAL_RECORD));

        // 진료 기록 업데이트
        MedicalRecord updatedRecord = new MedicalRecord(
                medicalRecord.getMedicalRecordId(), // 기존 ID 유지
                requestDto.getDescription(),
                requestDto.getConsultation(),
                medicalRecord.getPatient(), // 환자 정보 유지
                doctor, // 로그인한 의사 정보 유지
                requestDto.getTreatmentPlan(),
                requestDto.getDoctorComment()
        );

        // 진료 기록 저장
        MedicalRecord savedRecord = medicalRecordRepository.save(updatedRecord);

        return new DoctorMedicalRecordResponseDto(
                savedRecord.getMedicalRecordId(),
                savedRecord.getDescription(),
                savedRecord.getConsultation(),
                savedRecord.getPatient().getName(),
                savedRecord.getPatient().getId(),
                savedRecord.getTreatmentPlan(),
                savedRecord.getDoctorComment()
        );
    }


    // 환자용 진료기록 수정
    @Transactional
    public PatientMedicalRecordResponseDto updatePatientMedicalRecord(
            Long medicalRecordId,
            PatientMedicalRecordRequestDto requestDto,
            AuthUser authUser) {
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        // 진료 기록 조회
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_MEDICAL_RECORD));

        MedicalRecord updatedRecord = new MedicalRecord(
                medicalRecord.getMedicalRecordId(), // 기존 ID 유지
                requestDto.getDescription(),
                requestDto.getConsultation(),
                medicalRecord.getPatient(),
                doctor,
                requestDto.getTreatmentPlan()
        );

        MedicalRecord savedRecord = medicalRecordRepository.save(updatedRecord);

        return new PatientMedicalRecordResponseDto(
                savedRecord.getMedicalRecordId(),
                savedRecord.getDescription(),
                savedRecord.getConsultation(),
                savedRecord.getDoctor().getName()
        );
    }


    // 진료기록 삭제
    @Transactional
    public void deleteMedicalRecord(Long medicalRecordId, AuthUser authUser) {
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        // 진료 기록 조회
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_MEDICAL_RECORD));

        // 의사가 이 진료 기록을 삭제할 수 있는지 확인
        if (!medicalRecord.getDoctor().equals(doctor)) {
            throw new ApiException(ErrorStatus._FORBIDDEN_ACCESS);
        }

        // 진료 기록 삭제
        medicalRecordRepository.delete(medicalRecord);
    }
}


