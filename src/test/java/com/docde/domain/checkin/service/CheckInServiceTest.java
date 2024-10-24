package com.docde.domain.checkin.service;

import com.docde.common.enums.Gender;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.util.ReflectionTestUtils.setField;


class CheckInServiceTest {

    @InjectMocks
    private CheckInService checkInService;

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private DoctorRepository doctorRepository;

    private CheckIn mokCheckIn;

    private Hospital mokHospital;

    private Doctor mokDoctor;

    private Patient mokPatient;

    private WeekTimetable mokWeekTimetable;

    private HospitalTimetable mokHospitalTimetable;
    private List<HospitalTimetable> mokHospitalTimetableList;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock data 설정
        mokPatient = Patient.builder()
                .phone("123-1234-1234")
                .name("환자이름")
                .gender(Gender.M)
                .address("주소")
                .build();

        setField(mokHospitalTimetable, "id", 1);
        setField(mokHospitalTimetable, "dayOfTheWeek", DayOfTheWeek.FRI);
        setField(mokHospitalTimetable, "openTime", LocalTime.of(9, 30));
        setField(mokHospitalTimetable, "closeTime", LocalTime.of(21, 30));
        setField(mokHospitalTimetable, "weekTimetable", mokWeekTimetable);

        mokHospitalTimetableList = new ArrayList<>();
        mokHospitalTimetableList.add(mokHospitalTimetable);

        setField(mokWeekTimetable, "Id", 1);
        setField(mokWeekTimetable, "hospital", mokHospital);
        setField(mokWeekTimetable, "hospitalTimetables", mokHospitalTimetableList);

        setField(mokHospital, "id", 1);
        setField(mokHospital, "name", "병원이름");
        setField(mokHospital, "address", "병원주소");
        setField(mokHospital, "contact", "이게뭔지모르겠어요");
        setField(mokHospital, "open_time", LocalTime.of(9, 30));
        setField(mokHospital, "closing_time", LocalTime.of(21, 30));
        setField(mokHospital, "weekTimetable", mokWeekTimetable);
        setField(mokHospital, "announcement", "병원안내");

        mokDoctor = Doctor.builder()
                .name("의사이름")
                .hospital(mokHospital)
                .description("의사설명")
                .build();

        mokCheckIn = CheckIn.builder()
                .patient(mokPatient)
                .doctor(mokDoctor)
                .checkinStatus(CheckinStatus.WAITING)
                .build();
    }

    @Test
    void saveCheckIn() {
    }

    @Test
    void getMyCheckIn() {
    }

    @Test
    void getAllCheckIns() {
    }

    @Test
    void updateCheckIn() {
    }

    @Test
    void deleteCheckIn() {
    }
}