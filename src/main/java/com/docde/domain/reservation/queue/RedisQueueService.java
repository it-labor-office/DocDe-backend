package com.docde.domain.reservation.queue;

import com.docde.domain.reservation.dto.ReservationPatientRequest;
import com.esotericsoftware.kryo.util.ObjectMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final QueueMetricsService queueMetricsService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReservationQueueManager reservationQueueManager;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

    private static final String QUEUE_KEY = "reservationQueue";

    public void enqueueRequest(ReservationPatientRequest.CreateReservation request) {
        try {
            String jsonRequest = objectMapper.writeValueAsString(request);
            redisTemplate.opsForList().leftPush(QUEUE_KEY, jsonRequest);

            // TTL 설정: 10분 후 자동 삭제
            redisTemplate.expire(QUEUE_KEY, Duration.ofMinutes(1));
            queueMetricsService.processQueueRequest(true); // 큐 추가 성공
            log.info("Added request to queue: {}", jsonRequest);

            // Queue Processor 트리거
            reservationQueueManager.triggerQueueProcessor(this); // RedisQueueService를 전달
        } catch (JsonProcessingException e) {
            queueMetricsService.processQueueRequest(false); // 큐 추가 실패
            log.error("Failed to serialize request: {}", e.getMessage());
            throw new RuntimeException("Serialization failed: ", e);
        }
    }

    public List<String> dequeueBatch(int batchSize) {
        List<Object> jsonRequests = redisTemplate.opsForList().rightPop(QUEUE_KEY, batchSize);
        log.info("Dequeued {} requests from queue.", jsonRequests.size());

        if (jsonRequests == null || jsonRequests.isEmpty()) {
            log.info("No data in queue to process.");
            return List.of();
        }

        log.info("Dequeued {} requests from queue.", jsonRequests.size());
        return jsonRequests.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}