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
    public void retry(){

        List<String> queue = redisTemplate.opsForList().range(WAITING_QUEUE_KEY, 0, 500);

        // 현 작업자가 500명 미만이고 대기열에 사람이 있으면
        if(getCurrentCount()<500 && queue != null){
            // 대기열의 500번과 그 이상인 유저만 남기고 모두 제거
            redisTemplate.opsForList().trim(WAITING_QUEUE_KEY, 500, -1);
            // 대기열의 0~499번째 집어 넣기
            for(String userId : queue){
                // 저장해 놓은 요청을 가져와서
                String value = redisTemplate.opsForValue().get("check in request of user " + userId);

                Long hospitalId = Long.valueOf(value.substring(0, value.indexOf("일")));
                Long doctorId = Long.valueOf(value.substring(value.indexOf("일")+1, value.indexOf("이")));
                String status = value.substring(value.indexOf("이")+1, value.indexOf("삼"));
                CheckInRequest checkInRequest = new CheckInRequest(doctorId, status);

                Long authUserId = Long.valueOf(value.substring(value.indexOf("삼")+1), value.indexOf("사"));
                Long patientId = Long.valueOf(value.substring(value.indexOf("사")+1));
                AuthUser authUser = AuthUser.builder().id(authUserId).patientId(patientId).build();

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
                + "일"
                + checkInRequest.getDoctorId().toString()
                + "이"
                + checkInRequest.getStatus()
                + "삼"
                + authUser.getId()
                + "사"
                + authUser.getPatientId();

        redisTemplate.opsForValue().set("check in request of user " + authUser.getId().toString(), value);
    }
    
    // 대기열 자체는 완성된 듯
    // 이제 대기하는 유저에게 남은 대기 인원 알림 실시간으로 보내기 만들면 됨
    // 할 수 있으면 예상 대기 시간도 계산해서 표시하는 로직을 짜고 싶은데... 어려워 보이니 일단 우선도는 후순위로
    // 재시도 메서드가 어차피 계속 반복되니 재시도 메서드에 남은 대기 인원 알림 보내기를 같이 넣으면 될 듯?
    // 남은 대기 인원 수가 아니라 대기열 내의 '내 순번'을 보내는게 맞을 듯
    // 방금 생각났는데 대기열의 순번이 온 사람들을 서비스로 집어 넣고 대기열에서 제거하는 로직이 없음. 추가해야함.
    // 바로 아래에 있는 pop 한번에 n명씩 꺼낼 수는 없나... 알아보기
    //==========================================

    // 대기열에 시간 두고 삭제 대신 수동 '대기열 나가기'를 넣는 것은?
    // 어차피 유저가 기다리고 있지 않아도 요청은 처리되는데(결과를 확인하지 못 할 뿐) 시간 삭제는 아예 없어도 될 듯?
    // 대기열 나가기도 없어도 될 듯??
    
    // 할 일 정리
    
    // 1. 재시도 하면서 남은 대기인원에게 남은 대기 인원 수(내 앞 사람의 수) 보내주기
    // 2. ★★순서가 되어 작업으로 들어간 유저 대기열에서 제거하는 로직 만들기★★ - 완료
    // 3. 웹소켓 관련 코드 흐름 숙지하기

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
