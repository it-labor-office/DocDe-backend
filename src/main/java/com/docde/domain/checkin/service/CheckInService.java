package com.docde.domain.checkin.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.aop.CurrentCount;
import com.docde.common.aop.DistributedLock;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.dto.CheckInResponse;
import com.docde.domain.checkin.dto.CheckInResponseOfPatient;
import com.docde.domain.checkin.entity.CheckIn;
import com.docde.domain.checkin.entity.CheckinStatus;
import com.docde.domain.checkin.repository.CheckInRepository;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.doctor.repository.DoctorRepository;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.repository.HospitalRepository;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.patient.repository.PatientRepository;
import com.docde.domain.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final PatientRepository patientRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 접수하기
    @Transactional
    @DistributedLock(key = "saveCheckIn", waitTime = 10, leaseTime = 5)
    @CurrentCount
    public CheckInResponse saveCheckIn(
            AuthUser authUser,
            Long hospitalId,
            CheckInRequest checkInRequest
    ) {

        // 존재하는 병원인지 확인
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_HOSPITAL));

        // 이미 진행중인 접수가 있으면 예외처리
        if (checkInRepository.checkCheckInExist(authUser.getPatientId())) {
            throw new ApiException(ErrorStatus._BAD_REQUEST_ALREADY_CHECKED_IN);
        }

        // 요청의 의사가 null이 아닐 경우, null일 경우
        if (checkInRequest.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(checkInRequest.getDoctorId())
                    .orElseThrow(() -> new ApiException(ErrorStatus._BAD_REQUEST_DOCTOR_NOT_BELONG_TO_HOSPITAL));

            // 해당 병원 소속 의사가 맞는지 확인
            if (!doctor.getHospital().equals(hospital)) {
                throw new ApiException(ErrorStatus._BAD_REQUEST_DOCTOR_NOT_BELONG_TO_HOSPITAL);
            }

            Patient patient = patientRepository.findByUser_Id(authUser.getId())
                    .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));

            Long num = getNum("number of hospital" + hospital.getId());

            CheckIn checkIn = CheckIn.builder()
                    .checkinStatus(CheckinStatus.WAITING)
                    .number(num)
                    .doctor(doctor)
                    .patient(patient)
                    .build();

            joinQueue(hospital.getId(), checkIn.getPatient().getName() + checkIn.getPatient().getId());

            // 최대 번호(다음 접수에 발급될 번호)
            setNum("number of hospital" + hospital.getId(), num + 1L);

            checkInRepository.save(checkIn);

            return checkInResponseFromCheckIn(checkIn);
        } else {
            Patient patient = patientRepository.findByUser_Id(authUser.getPatientId())
                    .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_PATIENT));

            Long num = getNum("number of hospital" + hospital.getId());

            CheckIn checkIn = CheckIn.builder()
                    .checkinStatus(CheckinStatus.WAITING)
                    .number(num)
                    .patient(patient)
                    .build();

            joinQueue(hospital.getId(), checkIn.getPatient().getName() + checkIn.getPatient().getId());

            // 최대 번호(다음 접수에 발급될 번호)
            setNum("number of hospital" + hospital.getId(), num + 1L);

            checkInRepository.save(checkIn);

            return checkInResponseFromCheckIn(checkIn);
        }
    }

    // 자신의 접수 상태 확인(사용자)
    public CheckInResponseOfPatient getMyCheckIn(AuthUser authUser, Long hospitalId) {

        // 로그인된 유저 id로 접수 찾기
        CheckIn checkIn = checkInRepository.findByPatientId(authUser.getPatientId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_CHECK_IN));

        Long queue = redisTemplate.opsForList().indexOf(
                "checkin queue of hospital " + hospitalId,
                checkIn.getPatient().getName() + checkIn.getPatient().getId()
        );

        return patientDtoFromCheckIn(checkIn, queue);
    }

    // 접수 목록만 확인(병원)
    public List<Object> getQueue(AuthUser authUser, Long hospitalId) {

        // 로그인된 유저 정보로 해당 병원 관계자인지 확인하기
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));
        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        return redisTemplate.opsForList().range("checkin queue of hospital " + hospitalId, 0, -1);
    }

    // 접수 상태 확인(병원)
    public List<CheckInResponse> getAllCheckIns(AuthUser authUser, Long hospitalId) {

        // 로그인된 유저 정보로 해당 병원 관계자인지 확인하기
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));
        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        List<Object> queue = redisTemplate.opsForList().range("checkin queue", 0, -1);

        // 해당 병원의 모든 접수 반환
        List<CheckIn> checkInList = checkInRepository.findAllByHospitalId(hospitalId);

        // 해당 접수 리스트를 접수 응답 dto 리스트로 바꿔 리턴
        List<CheckInResponse> checkInResponseList = new ArrayList<>();
        for (CheckIn checkIn : checkInList) {
            checkInResponseList.add(checkInResponseFromCheckIn(checkIn));
        }

        return checkInResponseList;
    }

    // 접수 상태 변경
    @Transactional
    public CheckInResponse updateCheckIn(
            AuthUser authUser,
            Long hospitalId,
            Long checkInId,
            CheckInRequest checkInRequest
    ) {

        // 로그인된 유저 정보로 해당 병원 관계자인지 확인하기
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));
        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_CHECK_IN));

        // 요청에 의사 아이디가 존재할 때
        if (checkInRequest.getDoctorId() != null) {
            Doctor addedDoctor = doctorRepository.findById(checkInRequest.getDoctorId())
                    .orElseThrow(() -> new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL));

            if (!addedDoctor.getHospital().getId().equals(hospitalId)) {
                throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
            }

            checkIn.updateDoctor(addedDoctor);
        }

        // 접수 상태 대기중이 아닐 때
        if (checkIn.getCheckinStatus() != CheckinStatus.WAITING) {
            throw new ApiException(ErrorStatus._ONLY_WAITING_CAN_CHANGED);
        }
        // 접수 상태 완료 혹은 취소로 변경
        if (checkInRequest.getStatus() != null) {

            if (checkInRequest.getStatus().equals("COMPLETED") || checkInRequest.getStatus().equals("CANCELED")) {
                throw new ApiException(ErrorStatus._INVALID_CHECK_IN_STATUS);
            }

            checkIn.updateStatus(CheckinStatus.valueOf(checkInRequest.getStatus()));

            exitQueue(hospitalId, checkIn.getPatient().getName() + checkIn.getPatient().getId());
        }

        return checkInResponseFromCheckIn(checkIn);
    }

    // 대기 번호 초기화하기
    @Transactional
    public void resetNumber(AuthUser authUser, Long hospitalId) {

        // 로그인된 유저 정보로 해당 병원 관계자인지 확인하기
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        // 지금 대기 중인 접수가 있으면 안 됨
        if (!checkInRepository.findAllWaitingByHospitalId(hospitalId).isEmpty()) {
            throw new ApiException(ErrorStatus._RESET_ONLY_EMPTY);
        }

        // 초기화하기
        setNum("number of hospital" + hospitalId, 0L);
    }

    // 접수 기록 영구 삭제
    @Transactional
    public void deleteCheckIn(AuthUser authUser, Long checkInId) {

        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_CHECK_IN));

        // 로그인된 유저의 소속 병원이 접수의 병원과 같은지 확인
        Doctor doctor = doctorRepository.findByUser_Id(authUser.getId())
                .orElseThrow(() -> new ApiException(ErrorStatus._NOT_FOUND_DOCTOR));

        if (!doctor.getHospital().equals(checkIn.getDoctor().getHospital())) {
            throw new ApiException(ErrorStatus._FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL);
        }

        checkInRepository.delete(checkIn);
    }

    // CheckInResponse 만들기
    private CheckInResponse checkInResponseFromCheckIn(CheckIn checkIn) {
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getPatient().getName(),
                checkIn.getDoctor() != null ? checkIn.getDoctor().getName() : "의사 배정 필요",
                checkIn.getCreatedAt(),
                checkIn.getCheckinStatus().toString()
        );
    }

    // 환자용
    private CheckInResponseOfPatient patientDtoFromCheckIn(CheckIn checkIn, Long queue) {
        return new CheckInResponseOfPatient(
                checkIn.getPatient().getName(),
                checkIn.getDoctor() != null ? checkIn.getDoctor().getName() : "의사 배정 필요",
                checkIn.getCreatedAt(),
                queue
        );
    }

    private Long getNum(String key) {
        String value = String.valueOf(redisTemplate.opsForValue().get(key));
        if (value == null) {
            value = "0";
        }
        return Long.parseLong(value);
    }

    private void setNum(String key, Long value) {
        redisTemplate.opsForValue().set(key, value.toString());
    }

    /**
     * 큐 규칙 : checkin queue of hospital 1 , 환자이름 1
     * 1은 각각 병원 아이디, 환자 아이디
     **/
    private void joinQueue(Long hospitalId, String value) {
        redisTemplate.opsForList().rightPush("checkin queue of hospital " + hospitalId, value);
    }

    private void exitQueue(Long hospitalId, String value) {
        redisTemplate.opsForList().remove("checkin queue of hospital " + hospitalId, 0, value);
    }
}
