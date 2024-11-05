package com.docde.domain.hospital.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.dto.TimetableDto;
import com.docde.domain.hospital.dto.request.*;
import com.docde.domain.hospital.dto.response.*;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.entity.HospitalDocument;
import com.docde.domain.hospital.entity.HospitalTimetable;
import com.docde.domain.hospital.repository.HospitalElasticSearchRepository;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.hospital.repository.HospitalTimetableRepository;
import lombok.RequiredArgsConstructor;
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
    private final DoctorRepository doctorRepository;
    private final HospitalElasticSearchRepository hospitalElasticSearchRepository;

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
    public HospitalPostResponseDto postHospital(HospitalPostRequestDto requestDto, AuthUser authUser) {
        //authUser의 아이디로 의사정보를 업데이트하기 위해 의사엔티티를 찾습니다.
        Doctor doctor = doctorRepository.findById(authUser.getDoctorId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR)
        );
        //병원 생성 후 저장
        Hospital hospital = new Hospital(requestDto);
        Hospital savedHospital = hospitalRepository.save(hospital);
        HospitalDocument hospitalDocument = HospitalDocument.from(hospital);
        hospitalElasticSearchRepository.save(hospitalDocument);

        //해당의사(병원장)은 이제부터 저장된병원소속
        doctor.addDoctorToHospital(savedHospital);
        return new HospitalPostResponseDto(hospital);
    }

    public HospitalGetResponseDto getHospital(Long hospitalId, AuthUser authUser) {

        Hospital hospital = findHospitalByHospitalIdAndCheckIsDeleted(hospitalId);

        return new HospitalGetResponseDto(hospital);
    }

    @Transactional
    public HospitalWeeklyTimetablePostResponseDto postWeeklyTimetable(
            HospitalWeeklyTimetablePostRequestDto requestDto
            , AuthUser authUser
            , Long hospitalId) {

        Hospital hospital = findHospitalByHospitalIdAndCheckIsDeleted(authUser.getHospitalId());
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
                                                                          AuthUser authUser,
                                                                          Long hospitalId) {
        Hospital hospital = findHospitalByHospitalIdAndCheckIsDeleted(authUser.getHospitalId());
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
    public HospitalUpdateResponseDto putHospital(HospitalUpdateRequestDto requestDto, Long hospitalId, AuthUser authUser) {
        Hospital hospital = findHospitalByHospitalIdAndCheckIsDeleted(authUser.getHospitalId());

        hospital.updateAll(requestDto);

        return new HospitalUpdateResponseDto(hospital);
    }

    @Transactional
    public HospitalDeleteResponseDto deleteHospital(AuthUser authUser) {
        Hospital hospital = findHospitalByHospitalIdAndCheckIsDeleted(authUser.getHospitalId());
        //소프트 삭제
        hospital.delete();
        return new HospitalDeleteResponseDto(hospital);
    }

    public Hospital findHospitalByHospitalIdAndCheckIsDeleted(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_HOSPITAL)
        );
        if (hospital.isDeleted()) {
            throw new ApiException(ErrorStatus._DELETED_HOSPITAL);
        }
        return hospital;
    }

    @Transactional
    public HospitalUpdateResponseDto patchHospital(HospitalUpdateRequestDto requestDto, Long hospitalId, AuthUser authUser) {
        Hospital hospital = findHospitalByHospitalIdAndCheckIsDeleted(authUser.getHospitalId());

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

    @Transactional
    public HospitalPostDoctorResponseDto addDoctorToHospital(Long hospitalId, HospitalPostDoctorRequestDto requestDto, AuthUser authUser) {
        //병원을 찾는다
        Hospital hospital = findHospitalByHospitalIdAndCheckIsDeleted(hospitalId);
        Doctor doctor = doctorRepository.findByUser_Email(requestDto.getDoctorEmail()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR)
        );
        //의사를 병원에 추가한다. 연관관계 설정
        doctor.addDoctorToHospital(hospital);

        return new HospitalPostDoctorResponseDto(doctor.getId(), doctor.getName(), hospital.getId(), hospital.getName());
    }
}
