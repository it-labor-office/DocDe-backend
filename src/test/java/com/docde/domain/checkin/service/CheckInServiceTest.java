package com.docde.domain.checkin.service;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.entity.CheckIn;
import com.docde.domain.checkin.entity.CheckinStatus;
import com.docde.domain.checkin.repository.CheckInRepository;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.entity.DayOfTheWeek;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalTimetable;
import com.docde.domain.hospital.entity.WeekTimetable;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.user.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private CheckIn mokCheckIn = new CheckIn();

    private Hospital mokHospital = new Hospital();

    private Doctor mokDoctor = new Doctor();

    private Patient mokPatient = new Patient();

    private User mokUserDoctor = new User();
    private User mokUserPatient = new User();

    private WeekTimetable mokWeekTimetable = new WeekTimetable();

    private HospitalTimetable mokHospitalTimetable = new HospitalTimetable();
    private List<HospitalTimetable> mokHospitalTimetableList;

    private UserDetailsImpl mokUserDetails;

    private UserDetailsImpl mokDoctorUserDetails;

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
        setField(mokHospitalTimetable, "weekTimetable", mokWeekTimetable);

        mokHospitalTimetableList = new ArrayList<>();
        mokHospitalTimetableList.add(mokHospitalTimetable);

        setField(mokWeekTimetable, "Id", 1L);
        setField(mokWeekTimetable, "hospital", mokHospital);
        setField(mokWeekTimetable, "hospitalTimetables", mokHospitalTimetableList);

        setField(mokHospital, "id", 1L);
        setField(mokHospital, "name", "병원이름");
        setField(mokHospital, "address", "병원주소");
        setField(mokHospital, "contact", "이게뭔지모르겠어요");
        setField(mokHospital, "open_time", LocalTime.of(9, 30));
        setField(mokHospital, "closing_time", LocalTime.of(21, 30));
        setField(mokHospital, "weekTimetable", mokWeekTimetable);
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

        mokUserDetails = new UserDetailsImpl(mokUserPatient);
        mokDoctorUserDetails = new UserDetailsImpl(mokUserDoctor);
    }

    @Test
    void saveCheckIn_의사를지목했을때() {
        // g
        setField(mokCheckInRequest, "doctorId", 1L);
        setField(mokCheckInRequest, "status", null);
        BDDMockito.given(hospitalRepository.findById(1L)).willReturn(Optional.of(mokHospital));
        BDDMockito.given(doctorRepository.findById(mokCheckInRequest.getDoctorId())).willReturn(Optional.of(mokDoctor));

        // w
        CheckInResponse checkInResponse = checkInService.saveCheckIn(mokUserDetails, 1L, mokCheckInRequest);

        // t
        Assertions.assertNotNull(checkInResponse);
    }

    @Test
    void saveCheckIn_의사를지목하지않았을때() {
        // g
        BDDMockito.given(hospitalRepository.findById(1L)).willReturn(Optional.of(mokHospital));

        // w
        CheckInResponse checkInResponse = checkInService.saveCheckIn(mokUserDetails, 1L, mokCheckInRequest);

        // t
        Assertions.assertNotNull(checkInResponse);
    }

    @Test
    void saveCheckIn_이미접수중일때() {
        // g
        List<Long> patientIdList = new ArrayList<>();
        patientIdList.add(1L);
        patientIdList.add(2L);

        BDDMockito.given(hospitalRepository.findById(1L)).willReturn(Optional.of(mokHospital));
        BDDMockito.given(checkInRepository.findPatientId()).willReturn(patientIdList);

        // w
        ApiException exception = assertThrows(ApiException.class, () -> {
            checkInService.saveCheckIn(mokUserDetails, 1L, mokCheckInRequest);
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
        setField(mokHospital2, "open_time", LocalTime.of(9, 30));
        setField(mokHospital2, "closing_time", LocalTime.of(21, 30));
        setField(mokHospital2, "weekTimetable", mokWeekTimetable);
        setField(mokHospital2, "announcement", "병원안내");

        Doctor doctor = new Doctor("의사이름2", "설명", mokHospital2);

        // w
        ApiException exception = assertThrows(ApiException.class, () -> {
            checkInService.saveCheckIn(mokUserDetails, 1L, mokCheckInRequest);
        });
        // t
        Assertions.assertEquals("해당 병원에 소속된 의사가 아닙니다.", exception.getErrorCode().getReasonHttpStatus().getMessage());
    }

    @Test
    void getMyCheckIn() {

        // g
        BDDMockito.given(checkInRepository.findByPatientId(mokUserDetails.getUser().getPatient().getId())).willReturn(Optional.of(mokCheckIn));
        // w
        CheckInResponse checkInResponse = checkInService.getMyCheckIn(mokUserDetails);
        // t
        Assertions.assertNotNull(checkInResponse);
    }

    @Test
    void getAllCheckIns() {

        // g
        BDDMockito.given(doctorRepository.findById(mokDoctorUserDetails.getUser().getDoctor().getId())).willReturn(Optional.of(mokDoctor));
        // w
        List<CheckInResponse> checkInResponseList = checkInService.getAllCheckIns(mokDoctorUserDetails, 1L);
        // t
        Assertions.assertNotNull(checkInResponseList);
    }

    @Test
    void getAllCheckIns_해당병원소속의사가아닐때() {

        // g
        Hospital otherHospital = new Hospital();
        setField(otherHospital, "id", 2L);
        
        Doctor notMyDoctor = Doctor.builder()
                .hospital(otherHospital)
                .build();

        BDDMockito.given(doctorRepository.findById(mokDoctorUserDetails.getUser().getDoctor().getId())).willReturn(Optional.of(notMyDoctor));
        // w
        ApiException exception = assertThrows(ApiException.class, () -> {
            checkInService.getAllCheckIns(mokDoctorUserDetails, 1L);
        });
        // t
        Assertions.assertEquals("해당 병원에 소속된 의사가 아닙니다.", exception.getErrorCode().getReasonHttpStatus().getMessage());
    }

    @Test
    void updateCheckIn() {
    }

    @Test
    void deleteCheckIn() {
    }
}