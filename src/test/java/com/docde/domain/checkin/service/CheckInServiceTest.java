package com.docde.domain.checkin.service;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.dto.CheckInResponseOfPatient;
import com.docde.domain.checkin.entity.CheckIn;
import com.docde.domain.checkin.entity.CheckinStatus;
import com.docde.domain.checkin.repository.CheckInRepository;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.entity.DayOfTheWeek;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalTimetable;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @InjectMocks
    private CheckInService checkInService;

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private CheckIn mokCheckIn = new CheckIn();

    private Hospital mokHospital = new Hospital();

    private Doctor mokDoctor = new Doctor();

    private Patient mokPatient = new Patient();

    private User mokUserDoctor = new User();
    private User mokUserPatient = new User();

    private HospitalTimetable mokHospitalTimetable = new HospitalTimetable();
    private List<HospitalTimetable> mokHospitalTimetableList;

    private AuthUser mockPatientAuthUser = new AuthUser(2L, "asdf@asdf.asdf", UserRole.ROLE_PATIENT, null, 1L, null);
    private AuthUser mockDoctorAuthUser = new AuthUser(1L, "zxcv@zxcv.zxcv", UserRole.ROLE_DOCTOR, 1L, null, 1L);

    private CheckInRequest mokCheckInRequest = new CheckInRequest();


    @BeforeEach
    void setUp() {

        // Mock data 설정
        mokPatient = Patient.builder()
                .phone("123-1234-1234")
                .name("환자이름")
                .gender(Gender.M)
                .address("주소")
                .build();

        setField(mokHospitalTimetable, "id", 1L);
        setField(mokHospitalTimetable, "dayOfTheWeek", DayOfTheWeek.FRI);
        setField(mokHospitalTimetable, "openTime", LocalTime.of(9, 30));
        setField(mokHospitalTimetable, "closeTime", LocalTime.of(21, 30));

        mokHospitalTimetableList = new ArrayList<>();
        mokHospitalTimetableList.add(mokHospitalTimetable);

        setField(mokHospital, "id", 1L);
        setField(mokHospital, "name", "병원이름");
        setField(mokHospital, "address", "병원주소");
        setField(mokHospital, "contact", "이게뭔지모르겠어요");
        setField(mokHospital, "openTime", LocalTime.of(9, 30));
        setField(mokHospital, "closingTime", LocalTime.of(21, 30));
        setField(mokHospital, "announcement", "병원안내");

        setField(mokDoctor, "id", 1L);
        mokDoctor = Doctor.builder()
                .name("의사이름")
                .hospital(mokHospital)
                .description("의사설명")
                .build();

        setField(mokPatient, "id", 1L);
        mokPatient = Patient.builder()
                .name("환자이름")
                .address("환자주소")
                .phone("123-1231-1231")
                .gender(Gender.M)
                .build();

        setField(mokCheckIn, "id", 1L);
        mokCheckIn = CheckIn.builder()
                .patient(mokPatient)
                .doctor(mokDoctor)
                .checkinStatus(CheckinStatus.WAITING)
                .number(1L)
                .build();

        setField(mokUserDoctor, "id", 1L);
        mokUserDoctor = User.builder()
                .email("e@ma.il")
                .password("1234")
                .userRole(UserRole.ROLE_DOCTOR)
                .doctor(mokDoctor)
                .build();

        setField(mokUserPatient, "id", 2L);
        mokUserPatient = User.builder()
                .email("2e@ma.il")
                .password("1234")
                .userRole(UserRole.ROLE_PATIENT)
                .patient(mokPatient)
                .doctor(null)
                .build();

        //redis

    }

    @Test
    void saveCheckIn_의사를지목했을때() {
        // g
        setField(mokCheckInRequest, "doctorId", 1L);
        setField(mokCheckInRequest, "status", null);

        BDDMockito.given(patientRepository.findByUser_Id(mockPatientAuthUser.getId())).willReturn(Optional.of(mokPatient));
        BDDMockito.given(hospitalRepository.findById(1L)).willReturn(Optional.of(mokHospital));
        BDDMockito.given(doctorRepository.findById(mokCheckInRequest.getDoctorId())).willReturn(Optional.of(mokDoctor));
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        BDDMockito.given(redisTemplate.opsForValue()).willReturn(valueOperations);
        BDDMockito.given(valueOperations.get("number of hospital1")).willReturn(1L);
        ListOperations<String, Object> listOperations = Mockito.mock(ListOperations.class);
        BDDMockito.given(redisTemplate.opsForList()).willReturn(listOperations);

        // w
        CheckInResponse checkInResponse = checkInService.saveCheckIn(mockPatientAuthUser, 1L, mokCheckInRequest);

        // t
        Assertions.assertNotNull(checkInResponse);
    }

    @Test
    void saveCheckIn_의사를지목하지않았을때() {
        // g
        BDDMockito.given(hospitalRepository.findById(1L)).willReturn(Optional.of(mokHospital));
        BDDMockito.given(patientRepository.findByUser_Id(mockPatientAuthUser.getPatientId())).willReturn(Optional.of(mokPatient));
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        BDDMockito.given(redisTemplate.opsForValue()).willReturn(valueOperations);
        BDDMockito.given(valueOperations.get("number of hospital1")).willReturn(1L);
        ListOperations<String, Object> listOperations = Mockito.mock(ListOperations.class);
        BDDMockito.given(redisTemplate.opsForList()).willReturn(listOperations);

        // w
        CheckInResponse checkInResponse = checkInService.saveCheckIn(mockPatientAuthUser, 1L, mokCheckInRequest);

        // t
        Assertions.assertNotNull(checkInResponse);
    }

    @Test
    void saveCheckIn_이미접수중일때() {
        // g
        BDDMockito.given(hospitalRepository.findById(1L)).willReturn(Optional.of(mokHospital));
        BDDMockito.given(checkInRepository.checkCheckInExist(1L)).willReturn(true);

        // w
        ApiException exception = assertThrows(ApiException.class, () -> {
            checkInService.saveCheckIn(mockPatientAuthUser, 1L, mokCheckInRequest);
        });
        // t
        Assertions.assertEquals("이미 진행중인 접수가 있습니다.", exception.getErrorCode().getReasonHttpStatus().getMessage());
    }

    @Test
    void saveCheckIn_해당병원의사가아닐때() {

        // g
        BDDMockito.given(hospitalRepository.findById(1L)).willReturn(Optional.of(mokHospital));

        setField(mokCheckInRequest, "doctorId", 1L);

        Hospital mokHospital2 = new Hospital();
        setField(mokHospital2, "id", 2L);
        setField(mokHospital2, "name", "병원이름");
        setField(mokHospital2, "address", "병원주소");
        setField(mokHospital2, "contact", "이게뭔지모르겠어요");
        setField(mokHospital2, "openTime", LocalTime.of(9, 30));
        setField(mokHospital2, "closingTime", LocalTime.of(21, 30));
        setField(mokHospital2, "announcement", "병원안내");

        Doctor doctor = Doctor.builder().name("의사이름2").description("설명").hospital(mokHospital2).build();

        // w
        ApiException exception = assertThrows(ApiException.class, () -> {
            checkInService.saveCheckIn(mockPatientAuthUser, 1L, mokCheckInRequest);
        });
        // t
        Assertions.assertEquals("해당 병원에 소속된 의사가 아닙니다.", exception.getErrorCode().getReasonHttpStatus().getMessage());
    }

    @Test
    void getMyCheckIn() {

        // g
        BDDMockito.given(checkInRepository.findByPatientId(mockPatientAuthUser.getPatientId())).willReturn(Optional.of(mokCheckIn));
        ListOperations<String, Object> listOperations = Mockito.mock(ListOperations.class);
        BDDMockito.given(redisTemplate.opsForList()).willReturn(listOperations);
        BDDMockito.given(listOperations.indexOf("checkin queue of hospital 1", "환자이름null")).willReturn(1L);

        // w
        CheckInResponseOfPatient checkInResponse = checkInService.getMyCheckIn(mockPatientAuthUser, 1L);
        // t
        Assertions.assertNotNull(checkInResponse);
    }

    @Test
    void getAllCheckIns() {

        // g
        List<Object> queue = new ArrayList<>();
        BDDMockito.given(doctorRepository.findByUser_Id(mockDoctorAuthUser.getId())).willReturn(Optional.of(mokDoctor));

        ListOperations<String, Object> listOperations = Mockito.mock(ListOperations.class);
        BDDMockito.given(redisTemplate.opsForList()).willReturn(listOperations);
        BDDMockito.given(listOperations.range("checkin queue", 0, -1)).willReturn(queue);

        // w
        List<CheckInResponse> checkInResponseList = checkInService.getAllCheckIns(mockDoctorAuthUser, 1L);
        // t
        Assertions.assertNotNull(checkInResponseList);
    }

    @Test
    void getAllCheckIns_해당병원소속의사가아닐때() {

        // g
        Hospital otherHospital = new Hospital();
        setField(otherHospital, "id", 2L);

        // w
        ApiException exception = assertThrows(ApiException.class, () -> {
            checkInService.getAllCheckIns(mockDoctorAuthUser, 1L);
        });
        // t
        Assertions.assertEquals("의사를 찾을 수 없습니다.", exception.getErrorCode().getReasonHttpStatus().getMessage());
    }

    @Test
    void updateCheckIn_요청에의사가존재할때() {

        // g
        setField(mokCheckInRequest, "doctorId", 1L);
        setField(mokCheckInRequest, "status", null);
        BDDMockito.given(doctorRepository.findByUser_Id(mockDoctorAuthUser.getId())).willReturn(Optional.of(mokDoctor));
        BDDMockito.given(checkInRepository.findById(1L)).willReturn(Optional.of(mokCheckIn));
        BDDMockito.given(doctorRepository.findById(mokCheckInRequest.getDoctorId())).willReturn(Optional.of(mokDoctor));

        // w
        CheckInResponse checkInResponse = checkInService.updateCheckIn(mockDoctorAuthUser, 1L, 1L, mokCheckInRequest);

        // t
        Assertions.assertNotNull(checkInResponse);
    }

    @Test
    void updateCheckIn_요청에접수상태변경이존재할때() {

        // g
        setField(mokCheckInRequest, "doctorId", null);
        setField(mokCheckInRequest, "status", "WAITING");
        BDDMockito.given(doctorRepository.findByUser_Id(mockDoctorAuthUser.getDoctorId())).willReturn(Optional.of(mokDoctor));
        BDDMockito.given(checkInRepository.findById(1L)).willReturn(Optional.of(mokCheckIn));
        ListOperations<String, Object> listOperations = Mockito.mock(ListOperations.class);
        BDDMockito.given(redisTemplate.opsForList()).willReturn(listOperations);

        // w
        CheckInResponse checkInResponse = checkInService.updateCheckIn(mockDoctorAuthUser, 1L, 1L, mokCheckInRequest);

        // t
        Assertions.assertNotNull(checkInResponse);
    }

    @Test
    void deleteCheckIn() {

        // g
        checkInRepository.save(mokCheckIn);
        BDDMockito.given(doctorRepository.findByUser_Id(mockDoctorAuthUser.getId())).willReturn(Optional.of(mokDoctor));
        BDDMockito.given(checkInRepository.findById(1L)).willReturn(Optional.of(mokCheckIn));

        // w
        checkInService.deleteCheckIn(mockDoctorAuthUser, 1L);

        // t
        Assertions.assertEquals(0, checkInRepository.count());
    }
}