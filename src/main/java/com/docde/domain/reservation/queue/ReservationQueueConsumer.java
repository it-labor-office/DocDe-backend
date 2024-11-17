package com.docde.domain.reservation.queue;

import com.docde.domain.reservation.dto.ReservationPatientRequest;
import com.docde.domain.reservation.service.ReservationHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class ReservationQueueConsumer {

    private final RedisQueueService redisQueueService;
    private final ReservationHandler reservationHandler;
    private final ObjectMapper objectMapper;


    // @Scheduled 애너테이션을 사용함으로 인해 메서드를 호출할 필요없이, 자동으로 예약 요청을 꺼내와 실행됨.

    @Async("taskExecutor") // 병렬 처리
    @Scheduled(fixedDelay = 500) // 0.5초마다 실행
    public void processQueue() {
        // 한 번에 10개의 요청을 가져옴
        List<String> batchRequests = redisQueueService.dequeueBatch(10);

        if (!batchRequests.isEmpty()) {
            batchRequests.forEach(jsonRequest -> {
                try {
                    ReservationPatientRequest.CreateReservation request =
                            objectMapper.readValue(jsonRequest, ReservationPatientRequest.CreateReservation.class);

                    reservationHandler.handleReservation(
                            request.doctorId(),
                            request.reservationTime(),
                            request.reservationReason(),
                            request.patientId()
                    );
                } catch (JsonProcessingException e) {
                    log.error("역직렬화 실패: {}", e.getMessage());
                }
            });
        }
    }
}