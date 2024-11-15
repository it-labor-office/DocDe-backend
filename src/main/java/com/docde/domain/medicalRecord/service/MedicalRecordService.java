package com.docde.domain.medicalRecord.service;


import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.medicalRecord.dto.request.DoctorMedicalRecordRequestDto;
import com.docde.domain.medicalRecord.dto.response.DoctorMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.MedicalRecordResponseDto;
import com.docde.domain.medicalRecord.dto.response.PatientMedicalRecordResponseDto;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.medicalRecord.repository.MedicalRecordRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.medicalRecord.encryption.EncryptionService;
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
    private final EncryptionService encryptionService;

    @Transactional
    public MedicalRecordResponseDto createMedicalRecord(DoctorMedicalRecordRequestDto requestDto, AuthUser authUser) throws Exception {

        // 로그인한 의사 정보
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        // 환자 정보
        Patient patient = patientRepository.findById(requestDto.getPatientId()).orElseThrow(()
                -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));

        String encryptedPatientName = patient.getName().charAt(0) + encryptionService.encrypt(patient.getName().substring(1));
        String encryptedDoctorName = doctor.getName().charAt(0) + encryptionService.encrypt(doctor.getName().substring(1));
        String encryptedDescription = encryptionService.encrypt(requestDto.getDescription());
        String encryptedTreatmentPlan = encryptionService.encrypt(requestDto.getTreatmentPlan());
        String encryptedDoctorComment = encryptionService.encrypt(requestDto.getDoctorComment());

        // 암호화된 데이터로 MedicalRecord 엔티티 생성
        MedicalRecord medicalRecord = new MedicalRecord(
                encryptedDescription,
                requestDto.getTreatmentDate(),
                patient,
                doctor,
                encryptedTreatmentPlan,
                encryptedDoctorComment
        );

        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);

        DoctorMedicalRecordResponseDto doctorResponseDto = new DoctorMedicalRecordResponseDto(
                savedRecord.getMedicalRecordId(),
                encryptedDescription,
                savedRecord.getTreatmentDate(),
                encryptedPatientName,
                patient.getId(),
                encryptedTreatmentPlan,
                encryptedDoctorComment
        );

        // 환자용 진료 기록
        PatientMedicalRecordResponseDto patientResponseDto = new PatientMedicalRecordResponseDto(
                savedRecord.getMedicalRecordId(),
                encryptedDescription,
                savedRecord.getTreatmentDate(),
                encryptedDoctorName
        );

        return new MedicalRecordResponseDto(doctorResponseDto, patientResponseDto);
    }


    // 특정 진료기록 조회
    @Transactional(readOnly = true)
    public DoctorMedicalRecordResponseDto getSpecificDoctorMedicalRecord(AuthUser authUser, Long medicalRecordId,
                                                                         String description, String treatmentPlan,
                                                                         String doctorComment) throws Exception {

        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        MedicalRecord medicalRecord = medicalRecordRepository
                .findSpecificMedicalRecord(medicalRecordId, description, treatmentPlan, doctorComment)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_MEDICAL_RECORD));

        if (!medicalRecord.getDoctor().getId().equals(doctor.getId())) {
            throw new ApiException(ErrorStatus._UNAUTHORIZED_ACCESS_MEDICAL_RECORD);
        }

        String decryptedPatientName = medicalRecord.getPatient().getName().charAt(0)
                + encryptionService.decrypt(medicalRecord.getPatient().getName().substring(1));
        String decryptedDescription = encryptionService.decrypt(medicalRecord.getDescription());
        String decryptedTreatmentPlan = encryptionService.decrypt(medicalRecord.getTreatmentPlan());
        String decryptedDoctorComment = encryptionService.decrypt(medicalRecord.getDoctorComment());


        return new DoctorMedicalRecordResponseDto(
                medicalRecord.getMedicalRecordId(),
                decryptedDescription,  // 복호화된 설명
                medicalRecord.getTreatmentDate(),
                decryptedPatientName,
                medicalRecord.getPatient().getId(),
                decryptedTreatmentPlan,  // 복호화된 치료 계획
                decryptedDoctorComment   // 복호화된 의사 코멘트
        );
    }


    // 의사가 의사용 진료기록 조회
    public List<DoctorMedicalRecordResponseDto> getDoctorMedicalRecord(AuthUser authUser) {

        // 로그인한 의사 ID
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        // 의사 ID로 진료 기록 조회
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByDoctorId(doctor.getId());

        // 암호화된 필드 복호화하여 DTO 생성
        return medicalRecords.stream().map(record -> {
            String decryptedDescription = null;
            try {
                decryptedDescription = encryptionService.decrypt(record.getDescription());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String decryptedTreatmentPlan = null;
            try {
                decryptedTreatmentPlan = encryptionService.decrypt(record.getTreatmentPlan());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String decryptedDoctorComment = null;
            try {
                decryptedDoctorComment = encryptionService.decrypt(record.getDoctorComment());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 환자 이름 복호화
            String decryptedPatientName = null;
            try {
                decryptedPatientName = encryptionService.decrypt(record.getPatient().getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return new DoctorMedicalRecordResponseDto(
                    record.getMedicalRecordId(),
                    decryptedDescription,          // 복호화된 설명
                    record.getTreatmentDate(),      // 비암호화된 진료 날짜
                    decryptedPatientName,          // 복호화된 환자 이름
                    record.getPatient().getId(),    // 환자 ID
                    decryptedTreatmentPlan,         // 복호화된 치료 계획
                    decryptedDoctorComment          // 복호화된 의사 코멘트
            );
        }).collect(Collectors.toList());

    }


    // 환자가 자신의 진료기록 조회
    public List<PatientMedicalRecordResponseDto> getPatientMedicalRecord(AuthUser authUser) {

        // 로그인한 환자 ID로 환자 정보 조회
        Patient patient = patientRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));

        // 환자 ID로 진료 기록 조회
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPatientId(patient.getId());

        // 암호화된 필드 복호화 후 DTO 생성
        return medicalRecords.stream().map(record -> {
            String decryptedDescription;
            String decryptedDoctorName;

            // 진료 설명 복호화
            try {
                decryptedDescription = encryptionService.decrypt(record.getDescription());
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrypt description", e);
            }

            // 의사 이름 복호화
            try {
                decryptedDoctorName = encryptionService.decrypt(record.getDoctor().getName());
            } catch (Exception e) {
                throw new RuntimeException("Failed to decrypt doctor name", e);
            }

            return new PatientMedicalRecordResponseDto(
                    record.getMedicalRecordId(),
                    decryptedDescription,
                    record.getTreatmentDate(),
                    decryptedDoctorName // 복호화된 의사 이름 사용
            );
        }).collect(Collectors.toList());
    }


    // 진료기록 수정
    @Transactional
    public MedicalRecordResponseDto updateMedicalRecord(Long medicalRecordId,
                                                        DoctorMedicalRecordRequestDto doctorRequestDto,
                                                        AuthUser authUser) throws Exception {

        // 로그인한 의사 정보 확인
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        // 수정할 진료기록 조회
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_MEDICAL_RECORD));

        // 새로운 값으로 교체하며, 필요한 경우 암호화
        String encryptedDescription = doctorRequestDto.getDescription() != null
                ? encryptionService.encrypt(doctorRequestDto.getDescription())
                : medicalRecord.getDescription();

        String encryptedTreatmentPlan = doctorRequestDto.getTreatmentPlan() != null
                ? encryptionService.encrypt(doctorRequestDto.getTreatmentPlan())
                : medicalRecord.getTreatmentPlan();

        String encryptedDoctorComment = doctorRequestDto.getDoctorComment() != null
                ? encryptionService.encrypt(doctorRequestDto.getDoctorComment())
                : medicalRecord.getDoctorComment();

        // MedicalRecord 엔티티 업데이트
        MedicalRecord updatedRecord = new MedicalRecord(
                medicalRecord.getMedicalRecordId(),
                encryptedDescription,
                doctorRequestDto.getTreatmentDate() != null ? doctorRequestDto.getTreatmentDate() : medicalRecord.getTreatmentDate(),
                medicalRecord.getPatient(),
                doctor,
                encryptedTreatmentPlan,
                encryptedDoctorComment
        );

        medicalRecordRepository.save(updatedRecord);

        // 복호화하여 응답 생성
        String decryptedDescription = encryptionService.decrypt(updatedRecord.getDescription());
        String decryptedTreatmentPlan = encryptionService.decrypt(updatedRecord.getTreatmentPlan());
        String decryptedDoctorComment = encryptionService.decrypt(updatedRecord.getDoctorComment());
        String decryptedDoctorName = encryptionService.decrypt(doctor.getName());
        String decryptedPatientName = encryptionService.decrypt(medicalRecord.getPatient().getName());

        DoctorMedicalRecordResponseDto doctorResponse = new DoctorMedicalRecordResponseDto(
                updatedRecord.getMedicalRecordId(),
                decryptedDescription,
                updatedRecord.getTreatmentDate(),
                decryptedPatientName,  // 복호화된 환자 이름 사용
                medicalRecord.getPatient().getId(),
                decryptedTreatmentPlan,
                decryptedDoctorComment
        );

        PatientMedicalRecordResponseDto patientResponse = new PatientMedicalRecordResponseDto(
                updatedRecord.getMedicalRecordId(),
                decryptedDescription,
                updatedRecord.getTreatmentDate(),
                decryptedDoctorName // 복호화된 의사 이름 사용
        );

        return new MedicalRecordResponseDto(doctorResponse, patientResponse);
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


