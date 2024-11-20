package com.docde.domain.medicalRecord.service;

import com.docde.common.response.ErrorStatus;
import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
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
import com.docde.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordServiceTest {

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private EncryptionService encryptionService;

    private User user;
    private AuthUser authUser;
    private Doctor doctor;
    private Patient patient;
    private DoctorMedicalRecordRequestDto requestDto;
    private MedicalRecord medicalRecord;


    @BeforeEach
    void setUp() {

        doctor = new Doctor("testname", "test description", null, null);
        ReflectionTestUtils.setField(doctor, "id", 1L);

        patient = new Patient("testname", "oo시 oo구", "000-0000-0000", Gender.M, null);
        ReflectionTestUtils.setField(patient, "id", 1L);


        user = User.builder()
                .email("test@example.com")
                .password("password")
                .userRole(UserRole.ROLE_PATIENT)
                .doctor(null)
                .patient(null)
                .build();

        try {
            Field idField = user.getClass().getDeclaredField("id");
            idField.setAccessible(true); // private 접근 허용
            idField.set(user, 1L);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        authUser = AuthUser.builder()
                .id(1L)
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .doctorId(user.getDoctor() != null ? user.getDoctor().getId() : null)
                .patientId(user.getPatient() != null ? user.getPatient().getId() : null)
                .hospitalId(null)
                .build();


        medicalRecord = new MedicalRecord(
                1L,
                "Initial description",
                LocalDateTime.now(),
                patient,
                doctor,
                "Initial treatment plan",
                "Initial comment"
        );
    }

    @Test
    public void 진료기록_생성_성공_암호화() throws Exception {

        // given
        DoctorMedicalRecordRequestDto requestDto = new DoctorMedicalRecordRequestDto(
                doctor.getId(),
                patient.getId(),
                "Initial description",
                LocalDateTime.now(),
                "Initial treatment plan",
                "Initial comment"
        );

        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(patientRepository.findById(requestDto.getPatientId())).willReturn(Optional.of(patient));

        // 암호화된 데이터 모의 반환값 설정
        given(encryptionService.encrypt("estname")).willReturn("Encrypted estname"); // 이름의 첫 글자 제외
        given(encryptionService.encrypt("Initial description")).willReturn("Encrypted description");
        given(encryptionService.encrypt("Initial treatment plan")).willReturn("Encrypted treatment plan");
        given(encryptionService.encrypt("Initial comment")).willReturn("Encrypted comment");

        // 저장된 암호화된 MedicalRecord 객체
        MedicalRecord savedRecord = new MedicalRecord(
                "Encrypted description",
                requestDto.getTreatmentDate(),
                patient,
                doctor,
                "Encrypted treatment plan",
                "Encrypted comment"
        );
        given(medicalRecordRepository.save(any(MedicalRecord.class))).willReturn(savedRecord);

        // when
        MedicalRecordResponseDto responseDto = medicalRecordService.createMedicalRecord(requestDto, authUser);

        // then
        assertEquals(savedRecord.getMedicalRecordId(), responseDto.getDoctorRecord().getMedicalRecordId());
        assertEquals("Encrypted description", responseDto.getDoctorRecord().getDescription());
        assertEquals(savedRecord.getTreatmentDate(), responseDto.getDoctorRecord().getTreatmentDate());
        assertEquals("tEncrypted estname", responseDto.getDoctorRecord().getPatientName());
        assertEquals(patient.getId(), responseDto.getDoctorRecord().getPatientId());
        assertEquals("Encrypted treatment plan", responseDto.getDoctorRecord().getTreatmentPlan());
        assertEquals("Encrypted comment", responseDto.getDoctorRecord().getDoctorComment());

        assertEquals(savedRecord.getMedicalRecordId(), responseDto.getPatientRecord().getMedicalRecordId());
        assertEquals("Encrypted description", responseDto.getPatientRecord().getDescription());
        assertEquals(savedRecord.getTreatmentDate(), responseDto.getPatientRecord().getTreatmentDate());
        assertEquals("tEncrypted estname", responseDto.getPatientRecord().getDoctorName());

    }


    @Test
    public void 진료기록_생성_실패_의사_정보_없음() {
        // given
        DoctorMedicalRecordRequestDto requestDto = new DoctorMedicalRecordRequestDto(
                doctor.getId(),
                patient.getId(),
                "Description",
                LocalDateTime.now(),
                "Treatment Plan",
                "Doctor Comment"
        );

        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            medicalRecordService.createMedicalRecord(requestDto, authUser);
        });

        assertEquals(ErrorStatus._NOT_FOUND_DOCTOR, exception.getErrorCode());
    }


    @Test
    public void 진료기록_생성_실패_환자_정보_없음() {
        // given
        DoctorMedicalRecordRequestDto requestDto = new DoctorMedicalRecordRequestDto(
                doctor.getId(),
                patient.getId(),
                "Description",
                LocalDateTime.now(),
                "Treatment Plan",
                "Doctor Comment"
        );

        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(patientRepository.findById(requestDto.getPatientId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            medicalRecordService.createMedicalRecord(requestDto, authUser);
        });

        assertEquals(ErrorStatus._NOT_FOUND_PATIENT, exception.getErrorCode());
    }


    @Test
    public void 특정_진료기록_정상조회() throws Exception {
        // given
        Long medicalRecordId = 1L;
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));

        // 암호화된 데이터를 복호화하여 설정 (환자 이름 제외)
        given(encryptionService.decrypt("Encrypted description")).willReturn("Decrypted description");
        given(encryptionService.decrypt("Encrypted treatment plan")).willReturn("Decrypted treatment plan");
        given(encryptionService.decrypt("Encrypted doctor comment")).willReturn("Decrypted doctor comment");
        given(encryptionService.decrypt("estname")).willReturn("estname"); // 이름의 일부 복호화

        // 암호화된 MedicalRecord 설정
        MedicalRecord encryptedMedicalRecord = new MedicalRecord(
                medicalRecordId,
                "Encrypted description",
                medicalRecord.getTreatmentDate(),
                patient,
                doctor,
                "Encrypted treatment plan",
                "Encrypted doctor comment"
        );

        given(medicalRecordRepository.findSpecificMedicalRecord(medicalRecordId, null, null, null))
                .willReturn(Optional.of(encryptedMedicalRecord));

        // when
        DoctorMedicalRecordResponseDto response = medicalRecordService.getSpecificDoctorMedicalRecord(
                authUser, medicalRecordId, null, null, null);

        // then
        assertNotNull(response);
        assertEquals(medicalRecordId, response.getMedicalRecordId());
        assertEquals("Decrypted description", response.getDescription()); // 복호화된 설명 확인
        assertEquals(medicalRecord.getTreatmentDate(), response.getTreatmentDate());
        assertEquals(patient.getName(), response.getPatientName()); // 환자 이름은 복호화하지 않은 원본 값
        assertEquals(patient.getId(), response.getPatientId());
        assertEquals("Decrypted treatment plan", response.getTreatmentPlan()); // 복호화된 치료 계획 확인
        assertEquals("Decrypted doctor comment", response.getDoctorComment()); // 복호화된 의사 코멘트 확인
    }


    @Test
    public void 진료기록_조회_실패_의사_존재하지않음() {
        // given
        Long medicalRecordId = 1L;
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            medicalRecordService.getSpecificDoctorMedicalRecord(authUser, medicalRecordId, null, null, null);
        });
        assertEquals(ErrorStatus._NOT_FOUND_DOCTOR, exception.getErrorCode());
    }


    @Test
    public void 진료기록_조회_실패_진료기록_존재하지않음() {
        // given
        Long medicalRecordId = 1L;
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findSpecificMedicalRecord(medicalRecordId, null, null, null)).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            medicalRecordService.getSpecificDoctorMedicalRecord(authUser, medicalRecordId, null, null, null);
        });
        assertEquals(ErrorStatus._NOT_FOUND_MEDICAL_RECORD, exception.getErrorCode());
    }


    @Test
    public void 의사가_의사용_진료기록_조회() throws Exception {

        MedicalRecord medicalRecord2 = new MedicalRecord(
                2L,
                "Initial description2",
                LocalDateTime.now(),
                patient,
                doctor,
                "Initial treatment plan2",
                "Initial comment2"
        );

        given(encryptionService.decrypt("Initial description")).willReturn("Decrypted description");
        given(encryptionService.decrypt("Initial treatment plan")).willReturn("Decrypted treatment plan");
        given(encryptionService.decrypt("Initial comment")).willReturn("Decrypted comment");

        given(encryptionService.decrypt("Initial description2")).willReturn("Decrypted description2");
        given(encryptionService.decrypt("Initial treatment plan2")).willReturn("Decrypted treatment plan2");
        given(encryptionService.decrypt("Initial comment2")).willReturn("Decrypted comment2");

        given(encryptionService.decrypt("testname")).willReturn("Decrypted testname");

        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findByDoctorId(doctor.getId())).willReturn(Arrays.asList(medicalRecord, medicalRecord2));

        List<DoctorMedicalRecordResponseDto> response = medicalRecordService.getDoctorMedicalRecord(authUser);

        assertNotNull(response);
        assertEquals(2, response.size());

        // 첫 번째 진료 기록 검증
        DoctorMedicalRecordResponseDto record1 = response.get(0);
        assertEquals(medicalRecord.getMedicalRecordId(), record1.getMedicalRecordId());
        assertEquals("Decrypted description", record1.getDescription());
        assertEquals(medicalRecord.getTreatmentDate(), record1.getTreatmentDate());
        assertEquals("Decrypted testname", record1.getPatientName());
        assertEquals(medicalRecord.getPatient().getId(), record1.getPatientId());
        assertEquals("Decrypted treatment plan", record1.getTreatmentPlan());
        assertEquals("Decrypted comment", record1.getDoctorComment());

        // 두 번째 진료 기록 검증
        DoctorMedicalRecordResponseDto record2 = response.get(1);
        assertEquals(medicalRecord2.getMedicalRecordId(), record2.getMedicalRecordId());
        assertEquals("Decrypted description2", record2.getDescription());
        assertEquals(medicalRecord2.getTreatmentDate(), record2.getTreatmentDate());
        assertEquals("Decrypted testname", record2.getPatientName());
        assertEquals(medicalRecord2.getPatient().getId(), record2.getPatientId());
        assertEquals("Decrypted treatment plan2", record2.getTreatmentPlan());
        assertEquals("Decrypted comment2", record2.getDoctorComment());
    }


    @Test
    public void 의사가_의사_진료기록_조회_시_의사가_없는_경우_예외처리() {
        // given
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            medicalRecordService.getDoctorMedicalRecord(authUser);
        });
        assertEquals(ErrorStatus._NOT_FOUND_DOCTOR, exception.getErrorCode());
    }

    @Test
    public void 의사가_의사_진료기록_조회_시_진료기록이_없는_경우_예외처리() {
        // given
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findByDoctorId(doctor.getId())).willReturn(List.of());

        // when
        List<DoctorMedicalRecordResponseDto> response = medicalRecordService.getDoctorMedicalRecord(authUser);

        // then
        assertNotNull(response);
        assertTrue(response.isEmpty(), "진료 기록이 없음 -> 리스트는 비어야함");
    }


    @Test
    public void 환자가_자신의_진료기록을_정상적으로_조회() throws Exception {

        MedicalRecord medicalRecord2 = new MedicalRecord(
                2L,
                "Initial description2",
                LocalDateTime.now(),
                patient,
                doctor,
                "Initial treatment plan2",
                "Initial comment2"
        );

        // 모킹
        given(patientRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(patient));
        given(medicalRecordRepository.findByPatientId(patient.getId())).willReturn(List.of(medicalRecord, medicalRecord2));

        // 복호화 설정
        given(encryptionService.decrypt("Initial description")).willReturn("Decrypted description");
        given(encryptionService.decrypt("Initial description2")).willReturn("Decrypted description2");
        given(encryptionService.decrypt("testname")).willReturn("Decrypted doctor name");

        List<PatientMedicalRecordResponseDto> response = medicalRecordService.getPatientMedicalRecord(authUser);

        // 일치확인
        assertNotNull(response);
        assertEquals(2, response.size());

        // 첫 번째 진료 기록 검증
        PatientMedicalRecordResponseDto record1 = response.get(0);
        assertEquals(medicalRecord.getMedicalRecordId(), record1.getMedicalRecordId());
        assertEquals("Decrypted description", record1.getDescription());
        assertEquals(medicalRecord.getTreatmentDate(), record1.getTreatmentDate());
        assertEquals("Decrypted doctor name", record1.getDoctorName());

        // 두 번째 진료 기록 검증
        PatientMedicalRecordResponseDto record2 = response.get(1);
        assertEquals(medicalRecord2.getMedicalRecordId(), record2.getMedicalRecordId());
        assertEquals("Decrypted description2", record2.getDescription());
        assertEquals(medicalRecord2.getTreatmentDate(), record2.getTreatmentDate());
        assertEquals("Decrypted doctor name", record2.getDoctorName());
    }


    //        assertTrue(medicalRecord2.getConsultation().isAfter(LocalDateTime.now().plusHours(1))); -- 5분 기록보드
    @Test
    public void 환자가_자신의_진료기록_조회_시_환자가_없는_경우_예외처리() {
        // given
        given(patientRepository.findByUser_Id(authUser.getId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            medicalRecordService.getPatientMedicalRecord(authUser);
        });
        assertEquals(ErrorStatus._NOT_FOUND_PATIENT, exception.getErrorCode());
    }


    @Test
    public void 환자가_자신의_진료기록_조회_시_진료기록이_없는_경우_빈리스트_반환() {
        // given
        given(patientRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(patient));
        given(medicalRecordRepository.findByPatientId(patient.getId())).willReturn(List.of());

        // when
        List<PatientMedicalRecordResponseDto> response = medicalRecordService.getPatientMedicalRecord(authUser);

        // then
        assertNotNull(response);
        assertTrue(response.isEmpty(), "진료 기록이 없으므로 응답 리스트는 비어 있어야 합니다.");
    }

    @Test
    public void 진료기록_수정_성공() throws Exception {

        // given
        Long medicalRecordId = 1L;
        DoctorMedicalRecordRequestDto requestDto = new DoctorMedicalRecordRequestDto(
                doctor.getId(),
                null,
                "Updated description",
                LocalDateTime.now(),
                "Updated treatment plan",
                "Updated comment"
        );

        // Mock 설정
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findById(medicalRecordId)).willReturn(Optional.of(medicalRecord));

        // 암호화된 값 설정 (저장 시 사용)
        given(encryptionService.encrypt("Updated description")).willReturn("Encrypted description");
        given(encryptionService.encrypt("Updated treatment plan")).willReturn("Encrypted treatment plan");
        given(encryptionService.encrypt("Updated comment")).willReturn("Encrypted comment");

        // 복호화된 값 설정 (응답 시 사용)
        given(encryptionService.decrypt("Encrypted description")).willReturn("Updated description");
        given(encryptionService.decrypt("Encrypted treatment plan")).willReturn("Updated treatment plan");
        given(encryptionService.decrypt("Encrypted comment")).willReturn("Updated comment");

        // 의사와 환자 이름 복호화 설정
        given(encryptionService.decrypt(doctor.getName())).willReturn("Decrypted doctor name");
        given(encryptionService.decrypt(medicalRecord.getPatient().getName())).willReturn("Decrypted patient name");

        // MedicalRecord 객체가 save 메서드 호출 시 암호화된 값이 설정된 채로 반환되도록 설정
        MedicalRecord savedMedicalRecord = new MedicalRecord(
                medicalRecordId,
                "Encrypted description",
                requestDto.getTreatmentDate(),
                medicalRecord.getPatient(),
                doctor,
                "Encrypted treatment plan",
                "Encrypted comment"
        );
        given(medicalRecordRepository.save(any(MedicalRecord.class))).willReturn(savedMedicalRecord);

        // When: 진료 기록 업데이트 서비스 호출
        MedicalRecordResponseDto responseDto = medicalRecordService.updateMedicalRecord(medicalRecordId, requestDto, authUser);

        // Then: 응답 값이 예상대로 복호화된 값을 가지고 있는지 확인
        assertEquals(medicalRecordId, responseDto.getDoctorRecord().getMedicalRecordId());
        assertEquals("Updated description", responseDto.getDoctorRecord().getDescription());
        assertEquals("Updated treatment plan", responseDto.getDoctorRecord().getTreatmentPlan());
        assertEquals("Updated comment", responseDto.getDoctorRecord().getDoctorComment());
        assertEquals("Decrypted patient name", responseDto.getDoctorRecord().getPatientName());  // 복호화된 환자 이름 확인
    }


    @Test
    public void 진료기록_수정_시_실패_의사_존재하지않음() {
        // given
        Long medicalRecordId = 1L;
        DoctorMedicalRecordRequestDto requestDto = new DoctorMedicalRecordRequestDto(
                doctor.getId(),
                null,
                "Updated description",
                LocalDateTime.now(),
                "Updated treatment plan",
                "Updated comment"
        );

        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            medicalRecordService.updateMedicalRecord(medicalRecordId, requestDto, authUser);
        });
        assertEquals(ErrorStatus._NOT_FOUND_DOCTOR, exception.getErrorCode());
    }


    @Test
    public void 진료기록_수정_시_실패_진료기록_존재하지않음() {
        // given
        Long medicalRecordId = 1L;
        DoctorMedicalRecordRequestDto requestDto = new DoctorMedicalRecordRequestDto(
                doctor.getId(),
                null,
                "Updated description",
                LocalDateTime.now(),
                "Updated treatment plan",
                "Updated comment"
        );

        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findById(medicalRecordId)).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class, () -> {
            medicalRecordService.updateMedicalRecord(medicalRecordId, requestDto, authUser);
        });
        assertEquals(ErrorStatus._NOT_FOUND_MEDICAL_RECORD, exception.getErrorCode());
    }


    @Test
    void 진료기록_삭제_성공() {
        // given
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findById(medicalRecord.getMedicalRecordId())).willReturn(Optional.of(medicalRecord));

        // when
        assertDoesNotThrow(() -> medicalRecordService.deleteMedicalRecord(medicalRecord.getMedicalRecordId(), authUser));

        // then
        verify(medicalRecordRepository, times(1)).delete(medicalRecord);
    }


    @Test
    void 진료기록_삭제_시_존재하지않는_진료기록() {
        // given
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findById(medicalRecord.getMedicalRecordId())).willReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(ApiException.class,
                () -> medicalRecordService.deleteMedicalRecord(medicalRecord.getMedicalRecordId(), authUser));

        assertEquals(ErrorStatus._NOT_FOUND_MEDICAL_RECORD, exception.getErrorCode());
        verify(medicalRecordRepository, never()).delete(any());
    }


    @Test
    void 진료기록_삭제_시_권한없는_진료기록() {
        // given
        Doctor doctor2 = new Doctor("doctor1 test name", "test Description2", null, null);
        ReflectionTestUtils.setField(doctor, "id", 2L);

        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor2));
        given(medicalRecordRepository.findById(medicalRecord.getMedicalRecordId())).willReturn(Optional.of(medicalRecord));

        // when & then
        ApiException exception = assertThrows(ApiException.class,
                () -> medicalRecordService.deleteMedicalRecord(medicalRecord.getMedicalRecordId(), authUser));

        assertEquals(ErrorStatus._FORBIDDEN_ACCESS, exception.getErrorCode());
        verify(medicalRecordRepository, never()).delete(any());
    }
}
