package com.docde.domain.hospital.service;

import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.hospital.dto.TimetableDto;
import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalUpdateRequestDto;
import com.docde.domain.hospital.dto.request.HospitalWeeklyTimetablePostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalWeeklyTimetableUpdateRequestDto;
import com.docde.domain.hospital.dto.response.*;
import com.docde.domain.hospital.entity.DayOfTheWeek;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalTimetable;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.hospital.repository.HospitalTimetableRepository;
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


@ExtendWith(MockitoExtension.class)
public class HospitalTest {
    @InjectMocks
    private HospitalService hospitalService;
    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private HospitalTimetableRepository timetableRepository;

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
        public void 병원생성성공() {
            HospitalPostResponseDto responseDto = hospitalService.postHospital(requestDto, authUser);

            assertEquals(responseDto.getHospitalName(), "testHospitalName");
        }

        @Test
        public void 권한이_없어_병원_생성_실패() {
            //권한을 수정해 병원장이 아닌 의사로 병원생성시도
            ReflectionTestUtils.setField(authUser.getAuthorities(), "authorities", UserRole.ROLE_DOCTOR);

            ApiException exception = assertThrows(ApiException.class, () -> {
                hospitalService.postHospital(requestDto, authUser);
            });

            assertEquals("권한이 없습니다.", exception.getErrorCode().getReasonHttpStatus().getMessage());
        }

    }

    @Nested
    class 병원조회 {

        @Test
        public void 병원_조회_성공() {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "Id", 1L);

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
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "Id", 1L);

            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

            HospitalUpdateResponseDto responseDto = hospitalService.putHospital(putRequestDto, authUser);

            assertEquals(responseDto.getHospitalName(), "testputHospitalName");
            assertEquals(responseDto.getHospitalAddress(), "testputHospitalAddress");
            assertEquals(responseDto.getHospitalContact(), "testputHospitalContact");
            assertEquals(responseDto.getAnnouncement(), "testputannouncement");
        }

        @Test
        public void 병원_Patch_수정_성공() {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "Id", 1L);

            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
            ReflectionTestUtils.setField(patchRequestDto, "hospitalId", 1L);
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
            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.empty());

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
            Hospital hospital = new Hospital(requestDto);
            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

            HospitalDeleteResponseDto responseDto = hospitalService.deleteHospital(authUser);

            assertEquals(responseDto.getId(), hospital.getId());
        }

        @Test
        public void 병원을_찾지_못해_병원_삭제_실패() {
            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.empty());

            ApiException exception = assertThrows(ApiException.class, () -> {
                hospitalService.deleteHospital(authUser);
            });

            assertEquals("병원을 찾을 수 없습니다", exception.getErrorCode().getReasonHttpStatus().getMessage());
        }

//        @Test
//        public void 권한이_없어_병원을_삭제_하지_못함() {
//            ReflectionTestUtils.setField(authUser.getAuthorities(), "authorities", List.of(UserRole.ROLE_DOCTOR));
//            HospitalDeleteRequestDto deleteRequestDto = new HospitalDeleteRequestDto(1L);
//
//            ApiException exception = assertThrows(ApiException.class, () -> {
//                hospitalService.deleteHospital(deleteRequestDto, authUser);
//            });
//
//            assertEquals("권한이 없습니다.", exception.getErrorCode().getReasonHttpStatus().getMessage());
//        }
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
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "Id", 1L);
            HospitalWeeklyTimetablePostRequestDto timetablePostRequestDto =
                    new HospitalWeeklyTimetablePostRequestDto(timetableDtoList);
            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));

            HospitalWeeklyTimetablePostResponseDto responseDto = hospitalService.postWeeklyTimetable(
                    timetablePostRequestDto, authUser, 1L
            );

            assertEquals(responseDto.getTimetables().get(0).getDayOfTheWeek().name(), timetablePostRequestDto.getTimetables().get(0).getDayOfTheWeek().name());
        }

        @Test
        public void 병원_시간표_부분_수정() {
            Hospital hospital = new Hospital(requestDto);
            ReflectionTestUtils.setField(hospital, "Id", 1L);
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
            Mockito.when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
            HospitalWeeklyTimetableUpdateResponseDto responseDto = hospitalService.updateWeeklyTimetable(
                    updateRequestDto, authUser, 1L
            );

            assertEquals(responseDto.getTimetables().get(0).getOpenTime(), LocalTime.NOON);
            assertEquals(responseDto.getTimetables().get(0).getClosingTime(), LocalTime.MIDNIGHT);

            assertEquals(responseDto.getTimetables().get(3).getOpenTime(), LocalTime.NOON.minusHours(9));
            assertEquals(responseDto.getTimetables().get(3).getClosingTime(), LocalTime.MIDNIGHT.minusHours(9));

        }
    }
}
