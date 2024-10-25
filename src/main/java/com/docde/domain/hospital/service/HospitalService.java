package com.docde.domain.hospital.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.hospital.dto.TimetableDto;
import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalUpdateRequestDto;
import com.docde.domain.hospital.dto.request.HospitalWeeklyTimetablePostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalWeeklyTimetableUpdateRequestDto;
import com.docde.domain.hospital.dto.response.*;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalTimetable;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.hospital.repository.HospitalTimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {
    private final HospitalRepository hospitalRepository;
    private final HospitalTimetableRepository hospitalTimetableRepository;

//    @Transactional
//    public void ModifyingTest() {
//        Hospital hospital = new Hospital(
//                "name",
//                "address",
//                LocalTime.now(),
//                LocalTime.now(),
//                "contact",
//                "announcement"
//        );
//        Hospital savedhospital = hospitalRepository.save(hospital);
//        System.out.println("==== befor : " + hospital.getName());
//        hospitalRepository.updateHospitalName(savedhospital.getId(), "newName");
//        System.out.println("==== after : " + hospitalRepository.findById(savedhospital.getId()).get().getName());
//    }

    @Transactional
    public HospitalPostResponseDto postHospital(HospitalPostRequestDto requestDto, UserDetails userDetails) {
        //유저 권한 확인
        checkDoctorPresident(userDetails);
        //병원 갯수는 누구를 기준으로 할까??
        //받은 정보를 바탕으로 병원데이터생성
        Hospital hospital = new Hospital(requestDto);
        hospitalRepository.save(hospital);
        return new HospitalPostResponseDto(hospital);
    }

    public HospitalGetResponseDto getHospital(Long hospitalId, UserDetails userDetails) {

        Hospital hospital = findHospitalByHospitalId(hospitalId);

        return new HospitalGetResponseDto(hospital);
    }

    @Transactional
    public HospitalWeeklyTimetablePostResponseDto postWeeklyTimetable(
            HospitalWeeklyTimetablePostRequestDto requestDto,
            UserDetails userDetails, Long hospitalId) {
        checkDoctorPresident(userDetails);

        Hospital hospital = findHospitalByHospitalId(hospitalId);

        /*
        {
            "timetables":[
                {
                "dayOfTheWeek":"SUN","openTime":"12:00","closingTime":"13:00"},
                {
                "dayOfTheWeek":"MON","openTime":"12:00","closingTime":"13:00"},
                {
                "dayOfTheWeek":"TUE","openTime":"12:00","closingTime":"13:00"}
        requestDto의 모양
        **/
        //이걸 매핑해서 리스트로 변환
        List<HospitalTimetable> timetables = requestDto.getTimetables().stream().
                map(dto -> {
                    return new HospitalTimetable(
                            dto.getDayOfTheWeek(),
                            dto.getOpenTime(),
                            dto.getClosingTime(),
                            hospital);

                }).toList();
        //delete문이 7번날아가는 문제는 해결완료
        hospitalTimetableRepository.deleteAllByHospitalId(hospitalId);
        hospitalTimetableRepository.saveAll(timetables);
        List<TimetableDto> responseTimetableDtoList = timetables.stream().
                map(timetable -> {
                    return new TimetableDto(
                            timetable.getDayOfTheWeek(),
                            timetable.getOpenTime(),
                            timetable.getCloseTime());
                }).toList();
        return new HospitalWeeklyTimetablePostResponseDto(responseTimetableDtoList);
    }


    @Transactional
    public HospitalWeeklyTimetableUpdateResponseDto updateWeeklyTimetable(HospitalWeeklyTimetableUpdateRequestDto requestDto,
                                                                          UserDetails userDetails,
                                                                          Long hospitalId) {
        checkDoctorPresident(userDetails);

        Hospital hospital = findHospitalByHospitalId(hospitalId);
        //구 시간표
        List<HospitalTimetable> oldTimetables = hospitalTimetableRepository.findAllByHospitalId(hospital.getId());
        //변경 하려는 시간표
        List<HospitalTimetable> timetables = requestDto.getTimetables().stream().
                map(dto -> {
                    return new HospitalTimetable(
                            dto.getDayOfTheWeek(),
                            dto.getOpenTime(),
                            dto.getClosingTime(),
                            hospital);

                }).toList();

        List<HospitalTimetable> updateTimetables = oldTimetables.stream()
                .map(oldTimetable -> {
                    //구 시간표를 돌면서 현시간표와 "요일"이 같은 녀석들을 캐치
                    //같은녀석은 수정하려는 시간표의 날짜로 업데이트
                    //이중포문과 유사한 구조라고 할수 있음
                    Optional<HospitalTimetable> matchingTimetable = timetables.stream()
                            .filter(newTimetable -> newTimetable.getDayOfTheWeek().equals(
                                    oldTimetable.getDayOfTheWeek())).findFirst();
                    //구 시간표를 돌다가 수정하려는 현시간표의 요일과 같은녀석이 캐치되면
                    if (matchingTimetable.isPresent()) {
                        //해당되는 현시간표의 여는시각과 닫는시각으로 변경
                        HospitalTimetable newTimetable = matchingTimetable.get();
                        oldTimetable.updateOpenTime(newTimetable.getOpenTime());
                        oldTimetable.updateCloseTime(newTimetable.getCloseTime());
                    }
                    return oldTimetable;
                }).toList();
        //업데이트된 테이블로 업데이트
        hospitalTimetableRepository.saveAll(updateTimetables);
        //업데이트된 테이블을 Response에 담아서 return
        List<TimetableDto> responseTimetableDtoList = updateTimetables.stream().
                map(TimetableDto -> {
                    return new TimetableDto(
                            TimetableDto.getDayOfTheWeek(),
                            TimetableDto.getOpenTime(),
                            TimetableDto.getCloseTime());
                }).toList();
        return new HospitalWeeklyTimetableUpdateResponseDto(responseTimetableDtoList);
    }

    @Transactional
    public HospitalUpdateResponseDto putHospital(HospitalUpdateRequestDto requestDto, UserDetails userDetails) {
        checkDoctorPresident(userDetails);

        Hospital hospital = findHospitalByHospitalId(requestDto.getHospitalId());

        hospital.updateAll(requestDto);

        return new HospitalUpdateResponseDto(hospital);
    }

    @Transactional
    public HospitalDeleteResponseDto deleteHospital(HospitalDeleteRequestDto requestDto, UserDetails userDetails) {
        checkDoctorPresident(userDetails);

        Hospital hospital = findHospitalByHospitalId(requestDto.getHospitalId());

        hospitalRepository.delete(hospital);

        return new HospitalDeleteResponseDto(hospital);
    }

    public void checkDoctorPresident(UserDetails userDetails) {
        boolean check = userDetails.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_DOCTOR_PRESIDENT"));
        if (!check) {
            throw new ApiException(ErrorStatus._FORBIDDEN);
        }
    }

    public Hospital findHospitalByHospitalId(Long hospitalId) {
        return hospitalRepository.findById(hospitalId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_HOSPITAL)
        );
    }

    public HospitalUpdateResponseDto patchHospital(HospitalUpdateRequestDto requestDto, UserDetails userDetails) {
        checkDoctorPresident(userDetails);
        Hospital hospital = findHospitalByHospitalId(requestDto.getHospitalId());

        if (requestDto.getHospitalName() != null) {
            hospital.updateName(requestDto.getHospitalName());
        }
        if (requestDto.getHospitalAddress() != null) {
            hospital.updateAddress(requestDto.getHospitalAddress());
        }
        if (requestDto.getHospitalContact() != null) {
            hospital.updateContact(requestDto.getHospitalContact());
        }
        if (requestDto.getOpenTime() != null) {
            hospital.updateOpenTime(requestDto.getOpenTime());
        }
        if (requestDto.getClosingTime() != null) {
            hospital.updateClosingTime(requestDto.getClosingTime());
        }
        if (requestDto.getAnnouncement() != null) {
            hospital.updateAnnouncement(requestDto.getAnnouncement());
        }
        return new HospitalUpdateResponseDto(hospital);
    }
}
