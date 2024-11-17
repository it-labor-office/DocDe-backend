package com.docde.domain.reservation.queue;

import com.docde.domain.reservation.dto.ReservationPatientRequest;
import com.esotericsoftware.kryo.util.ObjectMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    // LocalDateTime, LocaDate 등 JSON 형식으로 직렬화하여 JSON에서 JAVA 객체로 변환가능
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String QUEUE_KEY = "reservationQueue"; // 큐 키


    // 큐에 예약 요청 추가
    public void enqueueRequest(ReservationPatientRequest.CreateReservation request) {
        try {
            // 객체를 json 문자열로 직렬화해해서 redis에 저장
            String jsonRequest = objectMapper.writeValueAsString(request);
            redisTemplate.opsForList().leftPush(QUEUE_KEY, jsonRequest);
            // opsForList : 레디스 리스트 타입 관련된 모든 작업 수행 -> 리스트에 값 추가 , 특정 인덱스에 접근하는 작업

        } catch (JsonProcessingException e) {
            throw new RuntimeException("요청에 대한 직렬화 실패", e);
        }
    }

    // 큐에서 예약 요청 꺼내기
    public List<String> dequeueBatch(int batchSize) {
        // Redis에서 지정된 batchSize만큼 데이터 가져오기
        List<Object> jsonRequests = redisTemplate.opsForList().rightPop(QUEUE_KEY, batchSize);
        if (jsonRequests != null && !jsonRequests.isEmpty()) {
            return jsonRequests.stream()
                    .map(Object::toString) // Object를 String으로 변환
                    .collect(Collectors.toList());
        }
        return List.of(); // 빈 리스트 반환
    }
}
