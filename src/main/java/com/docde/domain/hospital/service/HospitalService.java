package com.docde.domain.hospital.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.dto.response.ResponseDto;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalWeeklyTimetablePostRequestDto;
import com.docde.domain.hospital.dto.response.HospitalPostResponseDto;
import com.docde.domain.hospital.dto.response.HospitalWeeklyTimetablePostResponseDto;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalTimetable;
import com.docde.domain.hospital.entity.WeekTimetable;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.hospital.repository.WeekTimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalService {
    private final HospitalRepository hospitalRepository;
    private final WeekTimetableRepository weekTimetableRepository;
    @Transactional
    public HospitalPostResponseDto postHospital(HospitalPostRequestDto requestDto, UserDetails userDetails) {
        //유저 권한 확인
        boolean check = checkDoctorPresident(userDetails);
        //병원 갯수는 누구를 기준으로 할까??
        if(!check) {
            throw new ApiException(ErrorStatus._FORBIDDEN);
        }
        //받은 정보를 바탕으로 병원데이터생성
        Hospital hospital = new Hospital(requestDto);
        hospitalRepository.save(hospital);
        return new HospitalPostResponseDto(hospital);
    }

    public boolean checkDoctorPresident(UserDetails userDetails) {
        return userDetails.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_DOCTOR_PRESIDENT"));
    }
    @Transactional
    public HospitalWeeklyTimetablePostResponseDto postWeeklyTimetable(
            HospitalWeeklyTimetablePostRequestDto requestDto,
            UserDetails userDetails, Long hospitalId) {
        boolean check = checkDoctorPresident(userDetails);
        if(!check) {
            throw new ApiException(ErrorStatus._FORBIDDEN);
        }

        Hospital hospital = hospitalRepository.findById(hospitalId).orElseThrow(
                ()->new ApiException(ErrorStatus._NOT_FOUND_HOSPITAL)
        );

        WeekTimetable weekTimetable = new WeekTimetable(hospital);
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
                map(dto->{
                    return new HospitalTimetable(
                            dto.getDayOfTheWeek(),
                            dto.getOpenTime(),
                            dto.getClosingTime(),
                            weekTimetable);

                }).toList();
        //weakTimetable에 저장
        weekTimetable.setHospitalTimetableList(timetables);
        //병원 엔티티에서도 연관관계 주입
        hospital.addWeekTimetable(weekTimetable);
        weekTimetableRepository.save(weekTimetable);
        return new HospitalWeeklyTimetablePostResponseDto("시간표 생성완료");
    }
}
