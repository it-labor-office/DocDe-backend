package com.docde.domain.checkin.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
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

import java.util.ArrayList;
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

        // 존재하는 병원인지 확인
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(()->new ApiException(ErrorStatus._NOT_FOUND_HOSPITAL));

        // 이미 진행중인 접수가 있으면 예외처리
        if(checkInRepository.findPatientId().contains(hospitalId)){
            throw new ApiException(ErrorStatus._BAD_REQUEST_ALREADY_CHECKED_IN);
        }

        // 요청의 의사가 null이 아닐 경우, null일 경우
        if(checkInRequest.getDoctorId() != null){
            Doctor doctor = doctorRepository.findById(checkInRequest.getDoctorId())
                    .orElseThrow(()->new ApiException(ErrorStatus._BAD_REQUEST_DOCTOR_NOT_BELONG_TO_HOSPITAL));

            // 해당 병원 소속 의사가 맞는지 확인
            if(!doctor.getHospital().equals(hospital)){
                throw new ApiException(ErrorStatus._BAD_REQUEST_DOCTOR_NOT_BELONG_TO_HOSPITAL);
            }

            CheckIn checkIn = CheckIn.builder()
                    .checkinStatus(CheckinStatus.WAITING)
                    .doctor(doctor)
                    .patient(userDetails.getUser().getPatient())
                    .build();

            checkInRepository.save(checkIn);

            return checkInResponseFromCheckIn(checkIn);
        }else{
            CheckIn checkIn = CheckIn.builder()
                    .checkinStatus(CheckinStatus.PENDING)
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
                .orElseThrow(()-> new ApiException(ErrorStatus._NOT_FOUND_CHECK_IN));

        // 순서 구현되면 순서도 응답에 넣기
        return checkInResponseFromCheckIn(checkIn);
    }

    // 접수 상태 확인(병원)
    public List<CheckInResponse> getAllCheckIns(UserDetailsImpl userDetails, Long hospitalId) {

        // 로그인된 유저 정보로 해당 병원 관계자인지 확인하기
        Doctor doctor = doctorRepository.findById(userDetails.getUser().getDoctor().getId())
                .orElseThrow(()-> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        if(!doctor.getHospital().getId().equals(hospitalId)){
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        // 해당 병원의 모든 접수 반환
        List<CheckIn> checkInList = checkInRepository.findAllByHospitalId(hospitalId);

        // 해당 접수 리스트를 접수 응답 dto 리스트로 바꿔 리턴
        List<CheckInResponse> checkInResponseList = new ArrayList<>();
        for (CheckIn checkIn : checkInList){
            checkInResponseList.add(checkInResponseFromCheckIn(checkIn));
        }

        return checkInResponseList;
    }

    // 접수 상태 변경
    @Transactional
    public CheckInResponse updateCheckIn(UserDetailsImpl userDetails, Long hospitalId, Long checkInId, CheckInRequest checkInRequest) {

        // 로그인된 유저 정보로 해당 병원 관계자인지 확인하기
        Doctor doctor = doctorRepository.findById(userDetails.getUser().getDoctor().getId())
                .orElseThrow(()-> new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL));

        if(!doctor.getHospital().getId().equals(hospitalId)){
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(()-> new ApiException(ErrorStatus._NOT_FOUND_CHECK_IN));

        // 요청에 의사 아이디가 존재할 때
        if(checkInRequest.getDoctorId() != null){
            Doctor addedDoctor = doctorRepository.findById(checkInRequest.getDoctorId())
                    .orElseThrow(()-> new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL));

            if(!addedDoctor.getHospital().getId().equals(hospitalId)){
                throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
            }

            checkIn.updateDoctor(addedDoctor);
        }

        // 요청에 접수 상태가 존재할 때
        if(checkInRequest.getStatus() != null){
            checkIn.updateStatus(CheckinStatus.valueOf(checkInRequest.getStatus()));
        }

        return checkInResponseFromCheckIn(checkIn);
    }

    // 접수 기록 영구 삭제
    @Transactional
    public void deleteCheckIn(UserDetailsImpl userDetails, Long checkInId) {

        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(()-> new ApiException(ErrorStatus._NOT_FOUND_CHECK_IN));

        // 로그인된 유저의 소속 병원이 접수의 병원과 같은지 확인
        if(userDetails.getUser().getDoctor().getHospital().equals(checkIn.getDoctor().getHospital())){
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        checkInRepository.delete(checkIn);
    }

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
