package com.docde.domain.medicalRecord.service;

import com.docde.common.Apiresponse.ErrorStatus;
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
    public void 진료기록_생성_성공() {

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
        given(patientRepository.findById(requestDto.getPatientId())).willReturn(Optional.of(patient));

        MedicalRecord savedRecord = new MedicalRecord(
                requestDto.getDescription(),
                requestDto.getConsultation(),
                patient,
                doctor,
                requestDto.getTreatmentPlan(),
                requestDto.getDoctorComment()
        );


        given(medicalRecordRepository.save(any(MedicalRecord.class))).willReturn(savedRecord);

        // when
        MedicalRecordResponseDto responseDto = medicalRecordService.createMedicalRecord(requestDto, authUser);

        // then
        assertEquals(savedRecord.getMedicalRecordId(), responseDto.getDoctorRecord().getMedicalRecordId());
        assertEquals(savedRecord.getDescription(), responseDto.getDoctorRecord().getDescription());
        assertEquals(savedRecord.getConsultation(), responseDto.getDoctorRecord().getConsultation());
        assertEquals(patient.getName(), responseDto.getDoctorRecord().getPatientName());
        assertEquals(patient.getId(), responseDto.getDoctorRecord().getPatientId());
        assertEquals(savedRecord.getTreatmentPlan(), responseDto.getDoctorRecord().getTreatmentPlan());
        assertEquals(savedRecord.getDoctorComment(), responseDto.getDoctorRecord().getDoctorComment());

        assertEquals(savedRecord.getMedicalRecordId(), responseDto.getPatientRecord().getMedicalRecordId());
        assertEquals(savedRecord.getDescription(), responseDto.getPatientRecord().getDescription());
        assertEquals(savedRecord.getConsultation(), responseDto.getPatientRecord().getConsultation());
        assertEquals(doctor.getName(), responseDto.getPatientRecord().getDoctorName());
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
    public void 특정_진료기록_정상조회() {
        // given
        Long medicalRecordId = 1L;
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findSpecificMedicalRecord(medicalRecordId, null, null, null))
                .willReturn(Optional.of(medicalRecord));

        // when
        DoctorMedicalRecordResponseDto response = medicalRecordService.getSpecificDoctorMedicalRecord(
                authUser, medicalRecordId, null, null, null);

        // then
        assertNotNull(response);
        assertEquals(medicalRecord.getMedicalRecordId(), response.getMedicalRecordId());
        assertEquals(medicalRecord.getDescription(), response.getDescription());
        assertTrue(medicalRecord.getConsultation().isBefore(LocalDateTime.now().plusMinutes(1)));
        assertEquals(medicalRecord.getTreatmentPlan(), response.getTreatmentPlan());
        assertEquals(medicalRecord.getDoctorComment(), response.getDoctorComment());
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
    public void 의사가_의사용_진료기록_조회() {

        MedicalRecord medicalRecord2 = new MedicalRecord(
                2L,
                "Initial description2",
                LocalDateTime.now(),
                patient,
                doctor,
                "Initial treatment plan2",
                "Initial comment2"
        );


        // given
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findByDoctorId(doctor.getId())).willReturn(Arrays.asList(medicalRecord, medicalRecord2));

        // when
        List<DoctorMedicalRecordResponseDto> response = medicalRecordService.getDoctorMedicalRecord(authUser);

        // then
        assertNotNull(response);
        assertEquals(2, response.size());

        DoctorMedicalRecordResponseDto record1 = response.get(0);
        assertEquals(medicalRecord.getMedicalRecordId(), record1.getMedicalRecordId());
        assertEquals(medicalRecord.getDescription(), record1.getDescription());
        assertEquals(medicalRecord.getConsultation(), record1.getConsultation());
        assertEquals(medicalRecord.getPatient().getName(), record1.getPatientName());
        assertEquals(medicalRecord.getPatient().getId(), record1.getPatientId());
        assertEquals(medicalRecord.getTreatmentPlan(), record1.getTreatmentPlan());
        assertEquals(medicalRecord.getDoctorComment(), record1.getDoctorComment());

        DoctorMedicalRecordResponseDto record2 = response.get(1);
        assertEquals(medicalRecord2.getMedicalRecordId(), record2.getMedicalRecordId());
        assertEquals(medicalRecord2.getDescription(), record2.getDescription());
        assertEquals(medicalRecord2.getConsultation(), record2.getConsultation());
        assertEquals(medicalRecord2.getPatient().getName(), record2.getPatientName());
        assertEquals(medicalRecord2.getPatient().getId(), record2.getPatientId());
        assertEquals(medicalRecord2.getTreatmentPlan(), record2.getTreatmentPlan());
        assertEquals(medicalRecord2.getDoctorComment(), record2.getDoctorComment());
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
    public void 환자가_자신의_진료기록을_정상적으로_조회() {

        MedicalRecord medicalRecord2 = new MedicalRecord(
                2L,
                "Initial description2",
                LocalDateTime.now(),
                patient,
                doctor,
                "Initial treatment plan2",
                "Initial comment2"
        );
        given(patientRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(patient));
        given(medicalRecordRepository.findByPatientId(patient.getId())).willReturn(List.of(medicalRecord, medicalRecord2));

        // when
        List<PatientMedicalRecordResponseDto> response = medicalRecordService.getPatientMedicalRecord(authUser);

        // then
        assertNotNull(response);
        assertEquals(2, response.size());

        PatientMedicalRecordResponseDto record1 = response.get(0);
        assertEquals(medicalRecord.getMedicalRecordId(), record1.getMedicalRecordId());
        assertEquals(medicalRecord.getDescription(), record1.getDescription());
        assertEquals(medicalRecord.getConsultation(), record1.getConsultation());
        assertEquals(medicalRecord.getDoctor().getName(), record1.getDoctorName());

        // 두 번째 진료 기록 확인
        PatientMedicalRecordResponseDto record2 = response.get(1);
        assertEquals(medicalRecord2.getMedicalRecordId(), record2.getMedicalRecordId());
        assertEquals(medicalRecord2.getDescription(), record2.getDescription());
        assertEquals(medicalRecord2.getConsultation(), record2.getConsultation());
        assertEquals(medicalRecord2.getDoctor().getName(), record2.getDoctorName());
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
    public void 진료기록_수정_성공() {

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

        // mock 설정
        given(doctorRepository.findByUser_Id(authUser.getId())).willReturn(Optional.of(doctor));
        given(medicalRecordRepository.findById(medicalRecordId)).willReturn(Optional.of(medicalRecord));
        given(medicalRecordRepository.save(any(MedicalRecord.class))).willReturn(medicalRecord);

        // when
        MedicalRecordResponseDto responseDto = medicalRecordService.updateMedicalRecord(medicalRecordId, requestDto, authUser);

        // then
        assertEquals(medicalRecordId, responseDto.getDoctorRecord().getMedicalRecordId());
        assertEquals("Updated description", responseDto.getDoctorRecord().getDescription());
        assertEquals("Updated treatment plan", responseDto.getDoctorRecord().getTreatmentPlan());
        assertEquals("Updated comment", responseDto.getDoctorRecord().getDoctorComment());
        assertEquals(doctor.getName(), responseDto.getPatientRecord().getDoctorName());
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
