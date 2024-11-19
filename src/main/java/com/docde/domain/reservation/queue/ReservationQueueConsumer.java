package com.docde.domain.reservation.queue;

import com.docde.domain.reservation.dto.ReservationPatientRequest;
import com.docde.domain.reservation.service.ReservationHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationQueueConsumer {

    private final ReservationHandler reservationHandler;
    private final QueueMetricsService queueMetricsService;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4); // 병렬 워커
    private boolean isProcessorRunning = false; // 상태 플래그

    public synchronized void startQueueProcessor(RedisQueueService redisQueueService) {
        if (!isProcessorRunning) {
            log.info("Starting Queue Processor...");
            isProcessorRunning = true;

            executorService.scheduleAtFixedRate(() -> {
                try {
                    processQueue(50, redisQueueService); // 배치 크기
                } catch (Exception e) {
                    log.error("Queue processing error: {}", e.getMessage(), e);
                }
            }, 0, 500, TimeUnit.MILLISECONDS); // 0.5초마다 실행
        } else {
            log.info("Queue Processor is already running.");
        }
    }

    public void processQueue(int batchSize, RedisQueueService redisQueueService) {
        List<String> batchRequests = redisQueueService.dequeueBatch(batchSize);

        if (!batchRequests.isEmpty()) {
            log.info("Processing {} requests from the queue.", batchRequests.size());

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

                    queueMetricsService.processQueueRequest(true); // 큐 처리 성공

                } catch (JsonProcessingException e) {
                    queueMetricsService.processQueueRequest(false); // 역직렬화 실패

                    log.error("Deserialization failed: {}", e.getMessage());
                } catch (Exception e) {
                    queueMetricsService.processQueueRequest(false); // 예약 처리 실패

                    log.error("Failed to process reservation: {}", e.getMessage(), e);
                }
            });
        } else {
            log.info("No requests in the queue to process.");
        }
    }
}
