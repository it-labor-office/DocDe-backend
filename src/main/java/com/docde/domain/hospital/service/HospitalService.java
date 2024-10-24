package com.docde.domain.hospital.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalPutRequestDto;
import com.docde.domain.hospital.dto.request.HospitalWeeklyTimetablePostRequestDto;
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

@Service
@RequiredArgsConstructor
public class HospitalService {
    private final HospitalRepository hospitalRepository;
    private final HospitalTimetableRepository hospitalTimetableRepository;

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
        hospitalTimetableRepository.findByHospitalId(hospitalId);
        //insert문이 7번날아간다 최적화 필요
        hospital.updateTimetables(timetables);
        return new HospitalWeeklyTimetablePostResponseDto("시간표 생성완료");
    }

    @Transactional
    public HospitalPutResponseDto putHospital(HospitalPutRequestDto requestDto, UserDetails userDetails) {
        checkDoctorPresident(userDetails);

        Hospital hospital = findHospitalByHospitalId(requestDto.getHospitalId());

        hospital.updateAll(requestDto);

        return new HospitalPutResponseDto(hospital);
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
}
