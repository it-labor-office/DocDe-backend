/*
package com.docde.distributedlock;

import com.docde.common.aop.Lockable;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DistributedCounterService {

    private final DistributedCounterRepository distributedCounterRepository;


    @Transactional
    public void save(DistributedCounter counter) {
        distributedCounterRepository.save(counter);
    }

    @Transactional
    public Long getCounterIdForDoctor(Long doctorId, LocalDateTime reservationTime) {
        Optional<DistributedCounter> counterOpt = distributedCounterRepository.findByDoctorIdAndReservationTime(doctorId, reservationTime);

        // 카운터가 존재하면 그 ID를 반환
        if (counterOpt.isPresent()) {
            System.out.println(doctorId);
            return counterOpt.get().getId();
        }

        // 카운터가 없으면 새로 생성
        DistributedCounter newCounter = new DistributedCounter(doctorId, 1, reservationTime); // 기본 카운트를 1로 설정

        try {
            // 새로운 카운터 저장
            DistributedCounter savedCounter = distributedCounterRepository.save(newCounter);
            return savedCounter.getId();
        } catch (DataIntegrityViolationException e) {

            // 카운터가 중복으로 저장되는 경우 예외 처리
            throw new IllegalStateException("중복된 카운터가 존재합니다.");
        }
    }

    @Lockable
    @Transactional
    public void incrementCounter(Long doctorId, LocalDateTime reservationTime) {

        Long counterId = getCounterIdForDoctor(doctorId, reservationTime); // 의사 ID와 예약 시간에 맞는 카운터 ID 가져오기

        // 카운터 존재 여부 확인
        if (!distributedCounterRepository.existsById(counterId)) {
            throw new IllegalArgumentException("카운터가 존재하지 않습니다.");
        }

        // 카운터 증가
        int updatedCount = distributedCounterRepository.incrementDoctorReservationCount(counterId);

        if (updatedCount == 0) {
            throw new IllegalArgumentException("카운트를 찾을 수 없습니다. ID: " + counterId);
        }
    }
}*/
