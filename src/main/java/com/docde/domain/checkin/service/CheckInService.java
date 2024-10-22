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
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // 접수 상태 확인(병원)
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
