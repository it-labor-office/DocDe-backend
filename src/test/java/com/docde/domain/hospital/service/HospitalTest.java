package com.docde.domain.hospital.service;

import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.dto.TimetableDto;
import com.docde.domain.hospital.dto.request.*;
import com.docde.domain.hospital.dto.response.*;
import com.docde.domain.hospital.entity.DayOfTheWeek;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalTimetable;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.hospital.repository.HospitalTimetableRepository;
import com.docde.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
public class HospitalTest {
    @InjectMocks
    private HospitalService hospitalService;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private HospitalTimetableRepository timetableRepository;
    @Mock
    private DoctorRepository doctorRepository;

    AuthUser authUser = new AuthUser(1L, "testDoctor@gmail.com", UserRole.ROLE_DOCTOR_PRESIDENT, null, null, null);

    HospitalPostRequestDto requestDto = new HospitalPostRequestDto(
            "testHospitalName",
            "testHospitalAddress",
            "testHospitalContact",
            LocalTime.now(),
            LocalTime.now().minusHours(3),
            "testannouncement"
    );


    @Nested
    class 병원생성 {

        @Test
        @DisplayName("Hospital post success")
        public void 병원생성성공() {
            //G
            Doctor doctor = new Doctor();
            ReflectionTestUtils.setField(doctor, "id", 1L);
            ReflectionTestUtils.setField(doctor, "name", "testDoctorname");
            ReflectionTestUtils.setField(doctor, "description", "testDoctordescription");
            User user = new User("testemail", "testpassword", UserRole.ROLE_DOCTOR_PRESIDENT, doctor, null);
            ReflectionTestUtils.setField(doctor, "user", user);
            //W
            Mockito.when(doctorRepository.findById(authUser.getDoctorId())).thenReturn(Optional.of(doctor));
            Hospital hospital = new Hospital(requestDto);
            Mockito.when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);
            //T
            HospitalPostResponseDto responseDto = hospitalService.postHospital(requestDto, authUser);

            assertEquals(responseDto.getHospitalName(), "testHospitalName");
            assertEquals(doctor.getHospital().getName(), responseDto.getHospitalName());
        }

        @Test
        @DisplayName("Doctor not found")
        public void test2() {
            Doctor doctor = new Doctor();
            ReflectionTestUtils.setField(doctor, "id", 1L);
            ReflectionTestUtils.setField(doctor, "name", "testDoctorname");
            ReflectionTestUtils.setField(doctor, "description", "testDoctordescription");
            User user = new User("testemail", "testpassword", UserRole.ROLE_DOCTOR_PRESIDENT, doctor, null);
            ReflectionTestUtils.setField(doctor, "user", user);

            Mockito.when(doctorRepository.findById(authUser.getDoctorId())).thenReturn(Optional.empty());

            ApiException exception = assertThrows(ApiException.class, () -> hospitalService.postHospital(requestDto, authUser));

            assertEquals(exception.getErrorCode().getReasonHttpStatus().getMessage(), "의사를 찾을 수 없습니다.");
        }

    }

    @Nested
    class 병원조회 {

        @Test
        public void 병원_조회_성공() {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);

            Mockito.when(hospitalRepository.findById(hospital.getId())).thenReturn(Optional.of(hospital));

            HospitalGetResponseDto responseDto = hospitalService.getHospital(hospital.getId(), authUser);

            assertEquals(responseDto.getHospitalName(), "testHospitalName");
            assertEquals(responseDto.getHospitalId(), 1L);
        }

        @Test
        public void 병원을_찾지못해_조회_실패() {
            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.empty());

            ApiException exception = assertThrows(ApiException.class, () -> {
                hospitalService.getHospital(1L, authUser);
            });

            assertEquals("병원을 찾을 수 없습니다", exception.getErrorCode().getReasonHttpStatus().getMessage());
        }
    }

    @Nested
    class 병원수정 {
        HospitalUpdateRequestDto putRequestDto = new HospitalUpdateRequestDto(
                "testputHospitalName",
                "testputHospitalAddress",
                "testputHospitalContact",
                LocalTime.now(),
                LocalTime.now().minusHours(10),
                "testputannouncement"
        );


        HospitalUpdateRequestDto patchRequestDto = new HospitalUpdateRequestDto(
        );

        @Test
        public void 병원_Put_수정_성공() {
            ReflectionTestUtils.setField(authUser, "hospitalId", 1L);
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);

            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

            HospitalUpdateResponseDto responseDto = hospitalService.putHospital(putRequestDto, authUser);

            assertEquals(responseDto.getHospitalName(), "testputHospitalName");
            assertEquals(responseDto.getHospitalAddress(), "testputHospitalAddress");
            assertEquals(responseDto.getHospitalContact(), "testputHospitalContact");
            assertEquals(responseDto.getAnnouncement(), "testputannouncement");
        }

        @Test
        public void 병원_Patch_수정_성공() {
            ReflectionTestUtils.setField(authUser, "hospitalId", 1L);
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);

            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
            ReflectionTestUtils.setField(patchRequestDto, "hospitalName", "patchName");
            ReflectionTestUtils.setField(patchRequestDto, "hospitalAddress", "patchAddress");
            HospitalUpdateResponseDto responseDto = hospitalService.patchHospital(patchRequestDto, authUser);

            assertEquals(responseDto.getHospitalName(), "patchName");
            assertEquals(responseDto.getHospitalAddress(), "patchAddress");
            //수정하지않은 값
            assertEquals(responseDto.getHospitalContact(), "testHospitalContact");
        }

        @Test
        public void 병원을_찾지_못해_병원_수정_실패() {
            ReflectionTestUtils.setField(authUser, "hospitalId", 1L);
            Mockito.when(hospitalRepository.findById(authUser.getHospitalId())).thenReturn(Optional.empty());

            ApiException exception = assertThrows(ApiException.class, () -> {
                hospitalService.putHospital(putRequestDto, authUser);
            });

            assertEquals("병원을 찾을 수 없습니다", exception.getErrorCode().getReasonHttpStatus().getMessage());
        }
    }

    @Nested
    class 병원삭제 {

        @Test
        public void 병원삭제성공() {
            ReflectionTestUtils.setField(authUser, "hospitalId", 1L);
            Hospital hospital = new Hospital(requestDto);
            Mockito.when(hospitalRepository.findById(authUser.getHospitalId())).thenReturn(Optional.of(hospital));

            HospitalDeleteResponseDto responseDto = hospitalService.deleteHospital(authUser);

            assertEquals(responseDto.getId(), hospital.getId());
        }

        @Test
        public void 병원을_찾지_못해_병원_삭제_실패() {
            ReflectionTestUtils.setField(authUser, "hospitalId", 1L);
            Mockito.when(hospitalRepository.findById(authUser.getHospitalId())).thenReturn(Optional.empty());

            ApiException exception = assertThrows(ApiException.class, () -> {
                hospitalService.deleteHospital(authUser);
            });

            assertEquals("병원을 찾을 수 없습니다", exception.getErrorCode().getReasonHttpStatus().getMessage());
        }
    }

    @Nested
    class 병원_시간표 {
        TimetableDto timetableDto1 = new TimetableDto(DayOfTheWeek.SUN, LocalTime.now(), LocalTime.now().plusHours(9));
        TimetableDto timetableDto2 = new TimetableDto(DayOfTheWeek.MON, LocalTime.now(), LocalTime.now().plusHours(9));
        TimetableDto timetableDto3 = new TimetableDto(DayOfTheWeek.TUE, LocalTime.now(), LocalTime.now().plusHours(9));
        TimetableDto timetableDto4 = new TimetableDto(DayOfTheWeek.WED, LocalTime.now(), LocalTime.now().plusHours(9));
        TimetableDto timetableDto5 = new TimetableDto(DayOfTheWeek.THU, LocalTime.now(), LocalTime.now().plusHours(9));
        TimetableDto timetableDto6 = new TimetableDto(DayOfTheWeek.FRI, LocalTime.now(), LocalTime.now().plusHours(9));
        TimetableDto timetableDto7 = new TimetableDto(DayOfTheWeek.SAT, LocalTime.now(), LocalTime.now().plusHours(9));
        TimetableDto timetableDto8 = new TimetableDto(DayOfTheWeek.HOLIDAY, LocalTime.now(), LocalTime.now().plusHours(9));

        List<TimetableDto> timetableDtoList = List.of(timetableDto1, timetableDto2, timetableDto3,
                timetableDto4, timetableDto5, timetableDto6,
                timetableDto7, timetableDto8);

        @Test
        public void 병원_시간표_생성_완료() {
            ReflectionTestUtils.setField(authUser, "hospitalId", 1L);
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            HospitalWeeklyTimetablePostRequestDto timetablePostRequestDto =
                    new HospitalWeeklyTimetablePostRequestDto(timetableDtoList);
            Mockito.when(hospitalRepository.findById(authUser.getHospitalId())).thenReturn(Optional.of(hospital));

            HospitalWeeklyTimetablePostResponseDto responseDto = hospitalService.postWeeklyTimetable(
                    timetablePostRequestDto, authUser, 1L
            );

            assertEquals(responseDto.getTimetables().get(0).getDayOfTheWeek().name(), timetablePostRequestDto.getTimetables().get(0).getDayOfTheWeek().name());
        }

        @Test
        public void 병원_시간표_부분_수정() {
            ReflectionTestUtils.setField(authUser, "hospitalId", 1L);
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "id", 1L);
            //이미있던 시간표 생성용 RequestDto
            HospitalWeeklyTimetablePostRequestDto timetablePostRequestDto =
                    new HospitalWeeklyTimetablePostRequestDto(timetableDtoList);
            //부분수정할 시간표 생성용 RequestDto
            HospitalWeeklyTimetableUpdateRequestDto updateRequestDto =
                    new HospitalWeeklyTimetableUpdateRequestDto();

            TimetableDto updateSun = new TimetableDto(DayOfTheWeek.SUN, LocalTime.NOON, LocalTime.MIDNIGHT);
            TimetableDto updateWed = new TimetableDto(DayOfTheWeek.WED, LocalTime.NOON.minusHours(9), LocalTime.MIDNIGHT.minusHours(9));
            //부분수정할 시간표 생성용 RequestDto에 추가
            //일요일 시간표를 지금,지금-9 시에서 12:00 과 00:00 으로 변경하기
            updateRequestDto.init();
            updateRequestDto.add(updateSun);
            updateRequestDto.add(updateWed);
            List<HospitalTimetable> timetables = timetablePostRequestDto.getTimetables().stream().
                    map(dto -> {
                        return new HospitalTimetable(
                                dto.getDayOfTheWeek(),
                                dto.getOpenTime(),
                                dto.getClosingTime(),
                                hospital);

                    }).toList();

            Mockito.when(timetableRepository.findAllByHospitalId(hospital.getId())).thenReturn(timetables);
            Mockito.when(hospitalRepository.findById(authUser.getHospitalId())).thenReturn(Optional.of(hospital));
            HospitalWeeklyTimetableUpdateResponseDto responseDto = hospitalService.updateWeeklyTimetable(
                    updateRequestDto, authUser, 1L
            );

            assertEquals(responseDto.getTimetables().get(0).getOpenTime(), LocalTime.NOON);
            assertEquals(responseDto.getTimetables().get(0).getClosingTime(), LocalTime.MIDNIGHT);

            assertEquals(responseDto.getTimetables().get(3).getOpenTime(), LocalTime.NOON.minusHours(9));
            assertEquals(responseDto.getTimetables().get(3).getClosingTime(), LocalTime.MIDNIGHT.minusHours(9));

        }
    }

    @Nested
    class 의사_추가 {
        HospitalPostDoctorRequestDto addDoctorRequestDto = new HospitalPostDoctorRequestDto("addDoctor");

        @Test
        public void 의사_추가_성공() {
            ReflectionTestUtils.setField(authUser, "hospitalId", 1L);
            Doctor Presidentdoctor = new Doctor();
            ReflectionTestUtils.setField(Presidentdoctor, "id", 1L);
            User user = new User("testemail", "testpassword", UserRole.ROLE_DOCTOR_PRESIDENT, Presidentdoctor, null);
            ReflectionTestUtils.setField(Presidentdoctor, "user", user);

            Doctor addDoctor = new Doctor();
            ReflectionTestUtils.setField(addDoctor, "id", 2L);
            User adduser = new User("addDoctor", "addDcotor", UserRole.ROLE_DOCTOR, addDoctor, null);

            Hospital hospital = new Hospital(requestDto);

            Mockito.when(hospitalRepository.findById(authUser.getHospitalId())).thenReturn(Optional.of(hospital));
            Mockito.when(doctorRepository.findByUser_Email(addDoctorRequestDto.getDoctorEmail())).thenReturn(Optional.of(addDoctor));

            HospitalPostDoctorResponseDto responseDto = hospitalService.addDoctorToHospital(1L, addDoctorRequestDto, authUser);

            assertEquals(responseDto.getDoctorId(), addDoctor.getId());

        }
    }
}
