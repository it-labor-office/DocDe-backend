package com.docde.domain.checkin.service;

import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.entity.CheckIn;
import com.docde.domain.checkin.entity.CheckinStatus;
import com.docde.domain.checkin.repository.CheckInRepository;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    // 접수하기
    @Transactional
    public CheckInResponse saveCheckIn(
            UserDetailsImpl userDetails,
            Long hospitalId,
            CheckInRequest checkInRequest
    ) {

        // 이미 진행중인 접수가 있으면 예외처리
        // userId로 접수 찾기
        if(checkInRepository.findPatientId().contains(hospitalId)){
            throw new RuntimeException("커스텀오류로바꾸기, 이미 진행중인 접수가 있습니다.");
        }
        // 레포지토리에서 병원 찾기
        // 커스텀 예외 만들면 넣기
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow();

        // 요청의 의사가 null이 아닐 경우, null일 경우
        if(checkInRequest.getDoctorId() != null){
            Doctor doctor = doctorRepository.findById(checkInRequest.getDoctorId())
                    .orElseThrow();

            CheckIn checkIn = CheckIn.builder()
                    .checkinStatus(CheckinStatus.WAITING)
                    .doctor(doctor)
                    .patient(userDetails.getUser().getPatient())
                    .build();

            checkInRepository.save(checkIn);

            return checkInResponseFromCheckIn(checkIn);
        }else{
            CheckIn checkIn = CheckIn.builder()
                    .checkinStatus(CheckinStatus.WAITING)
                    .patient(userDetails.getUser().getPatient())
                    .build();

            checkInRepository.save(checkIn);

            return checkInResponseFromCheckIn(checkIn);
        }
    }

    // 자신의 접수 상태 확인(사용자)
    public CheckInResponse getMyCheckIn(UserDetailsImpl userDetails) {

        // 로그인된 유저 id로 접수 찾기. 예외처리필요
        CheckIn checkIn = checkInRepository.findByPatientId(userDetails.getUser().getPatient().getId())
                .orElseThrow();

        // 순서 구현되면 순서도 응답에 넣기
        return checkInResponseFromCheckIn(checkIn);
    }

    // 접수 상태 확인(병원)
    public List<CheckInResponse> getAllCheckIns(UserDetailsImpl userDetails, Long hospitalId) {

        // 로그인된 유저 정보로 해당 병원 관계자인지 확인하기
        // 유저의 의사를 병원이 가지고 있는지 확인하기
        // 해당 병원의 모든 접수 반환
        // 해당 접수 리스트를 접수 응답 dto 리스트로 바꿔 리턴
        // 병원, 의사 쪽 진행되면 하기
    }





    // 접수 상태 변경
    // 접수 기록 영구 삭제

    // CheckInResponse 만들기
    private CheckInResponse checkInResponseFromCheckIn(CheckIn checkIn){
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getPatient().getName(),
                checkIn.getDoctor().getName(),
                checkIn.getCreatedAt()
        );
    }



}
