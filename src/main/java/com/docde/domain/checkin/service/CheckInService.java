package com.docde.domain.checkin.service;

import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.entity.CheckIn;
import com.docde.domain.checkin.repository.CheckInRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;

    // 접수하기
    public CheckInResponse saveCheckIn(
            UserDetailsImpl userDetails,
            Long hospitalId,
            CheckInRequest checkInRequest
    ) {

        // 레포지토리에서 병원 찾기

        // 레포지토리에서 의사 찾기(의사가 null이 아닐 경우)
        if(checkInRequest.getDoctorId() != null){}


    }

    // 자신의 접수 상태 확인(사용자)
    // 접수 상태 확인(병원)
    // 접수 상태 변경
    // 접수 기록 영구 삭제

    // CheckInResponse 만들기
    public static CheckInResponse checkInResponseFromCheckIn(CheckIn checkIn){
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getPatient().getName(),
                checkIn.getDoctor().getName(),
                checkIn.getCreatedAt()
        );
    }



}
