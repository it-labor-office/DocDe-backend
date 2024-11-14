package com.docde.domain.queue.service;

import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final CheckInService checkInService;
    private final RedisTemplate<String, String> redisTemplate;
    public static final String CURRENT_COUNT_KEY = "current_count";
    private static final String WAITING_QUEUE_KEY = "waiting_queue";
    private static final int MAX_CONCURRENT_USERS = 100;
    private static final long QUEUE_ITEM_TTL_SECONDS = 300;

    // 요청 처리
    public boolean processRequest(AuthUser authUser, Long hospitalId, CheckInRequest checkInRequest){
        Integer currentCount = getCurrentCount();

        // 처리 중인 인원이 최대치보다 작고, 대기열이 없을 때 바로 통과
        if(currentCount<MAX_CONCURRENT_USERS && redisTemplate.opsForList().size(WAITING_QUEUE_KEY) != 0){
            return true;
        } else {
            // 바로 통과하지 못 할 상황이면 대기열에 집어 넣고 요청 내용을 저장
            addToWaitingQueue(authUser.getId().toString());
            recordRequest(authUser, hospitalId, checkInRequest);
            return false;
        }
    }

    // 상세 수치는 테스트 해 보고 수정하기
    @Scheduled(fixedRate = 10000)
    public void retry(@AuthenticationPrincipal AuthUser authUser){

        List<String> queue = redisTemplate.opsForList().range(WAITING_QUEUE_KEY, 0, 500);

        // 현 작업자가 500명 미만이고 대기열에 사람이 있으면
        if(getCurrentCount()<500 && queue != null){
            // 대기열의 0~499번째 집어 넣기
            for(String userId : queue){
                // 저장해 놓은 요청을 가져와서
                String value = redisTemplate.opsForValue().get("check in request of user " + userId);

                Long hospitalId = Long.valueOf(value.substring(0, value.indexOf("병")));
                Long doctorId = Long.valueOf(value.substring(value.indexOf("병")+1, value.indexOf("의")));
                String status = value.substring(value.indexOf("의")+1);
                CheckInRequest checkInRequest = new CheckInRequest(doctorId, status);

                // 바로 집어넣기
                checkInService.saveCheckIn(authUser, hospitalId, checkInRequest);
            }
        }
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

    // 대기열에 들어갈 때 요청 내용 저장하기
    private void recordRequest(AuthUser authUser, Long hospitalId, CheckInRequest checkInRequest){

        String value = hospitalId.toString()
                + "병"
                + checkInRequest.getDoctorId().toString()
                + "의"
                + checkInRequest.getStatus();

        redisTemplate.opsForValue().set("check in request of user " + authUser.getId().toString(), value);
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
