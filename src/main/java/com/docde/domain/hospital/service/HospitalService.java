package com.docde.domain.hospital.service;

import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.response.HospitalPostResponseDto;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    public HospitalPostResponseDto postHospital(HospitalPostRequestDto requestDto) {
        //유저 권한 확인

        //병원 갯수는 누구를 기준으로 할까??

        //받은 정보를 바탕으로 병원데이터생성
        Hospital hospital = new Hospital(requestDto);
        hospitalRepository.save(hospital);

        return new HospitalPostResponseDto(hospital);
    }
}
