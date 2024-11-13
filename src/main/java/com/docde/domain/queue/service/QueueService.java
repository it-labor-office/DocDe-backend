package com.docde.domain.queue.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String CURRENT_COUNT_KEY = "current_count";
    private static final String WAITING_QUEUE_KEY = "waiting_queue";
    private static final int MAX_CONCURRENT_USERS = 100;
    private static final long QUEUE_ITEM_TTL_SECONDS = 300;

    // 요청 처리
    public boolean processRequest(String userId){
        Integer currentCount = getCurrentCount();

        // 처리 중인 인원이 최대치보다 작을 때
        if(currentCount<MAX_CONCURRENT_USERS){
            incrementCurrentCount();
            return true;
        } else {
            addToWaitingQueue(userId);
            return false;
        }
    }

    // 작업 완료 후 처리
    public void finishRequest(){
        decrementCurrentCount();
        String nextUser = popFromWaitingQueue();
        if(nextUser != null){
            // 다음 유저에게 알림
            notifyUser(nextUser);
        }
    }

    // 반복 재시도
    @Scheduled(fixedRate = 10000)
    public void retry(){
        List<String> queue = redisTemplate.opsForList().range(WAITING_QUEUE_KEY, 0, -1);

        if(queue != null && !queue.isEmpty()){
            String nextUser = queue.get(0);

            if(canProcessRequest()){
                processRequest(nextUser);
                redisTemplate.opsForList().leftPop(WAITING_QUEUE_KEY);
            }
        }
    }

    // 현재 사용자 수 증가
    private void incrementCurrentCount() {
        redisTemplate.opsForValue().increment(CURRENT_COUNT_KEY);
    }

    // 현재 사용자 수 감소
    private void decrementCurrentCount() {
        redisTemplate.opsForValue().decrement(CURRENT_COUNT_KEY);
    }

    // 현재 사용자 수 가져오기
    private Integer getCurrentCount() {
        String count = redisTemplate.opsForValue().get(CURRENT_COUNT_KEY);
        return count == null ? 0 : Integer.parseInt(count);
    }

    // 대기열에 사용자 추가
    private void addToWaitingQueue(String userId) {
        redisTemplate.opsForList().rightPush(WAITING_QUEUE_KEY, userId);
        redisTemplate.expire(WAITING_QUEUE_KEY, Duration.ofSeconds(QUEUE_ITEM_TTL_SECONDS));
    }

    // 대기열에서 사용자 꺼내기
    private String popFromWaitingQueue() {
        return redisTemplate.opsForList().leftPop(WAITING_QUEUE_KEY);
    }

    // 대기가 끝났을 때 사용자에게 알림 전송
    private void notifyUser(String userId) {
        // 웹소켓을 통해 사용자에게 알림
        redisTemplate.convertAndSend("/queue/status", Map.of("userId", userId, "status", "READY"));
    }

    // 사용자에게 대기열 순번 정보를 웹소켓으로 전송
    private void notifyUserPosition(String userId, int position) {
        redisTemplate.convertAndSend("/queue/position", Map.of("userId", userId, "position", position));
    }

    // 대기열 사용자 처리 가능 여부 확인
    private boolean canProcessRequest() {
        // 처리 중인 사용자 수
        Long currentCount = redisTemplate.opsForList().size(CURRENT_COUNT_KEY);
        // 허용 범위 이내인지 확인
        return currentCount != null && currentCount < MAX_CONCURRENT_USERS;
    }

    /*// 대기열에 추가
    public void addToQueue(String userId) {
        redisTemplate.opsForList().rightPush(WAITING_QUEUE_KEY, userId);
    }

    // 대기열에서 제거
    public String popFromQueue() {
        return redisTemplate.opsForList().leftPop(WAITING_QUEUE_KEY);
    }

    // 대기열 내 위치 조회
    public int getPosition(String userId) {
        List<String> queue = redisTemplate.opsForList().range(WAITING_QUEUE_KEY, 0, -1);
        if (queue == null) return -1;
        return queue.indexOf(userId) + 1;
    }

    // 전체 대기열 크기 조회
    public long getQueueSize() {
        return redisTemplate.opsForList().size(WAITING_QUEUE_KEY);
    }*/
}
