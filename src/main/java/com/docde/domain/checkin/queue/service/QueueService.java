package com.docde.domain.checkin.queue.service;

import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.checkin.dto.CheckInRequest;
import com.docde.domain.checkin.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final CheckInService checkInService;
    private final RedisTemplate<String, String> redisTemplate;
    public static final String CURRENT_COUNT_KEY = "current_count";
    private static final String WAITING_QUEUE_KEY = "waiting_queue";
    private static final int MAX_CONCURRENT_USERS = 1000;
    private static final int DUMPED_USERS_TO_SERVICE = 500;

    // 요청 처리
    public boolean processRequest(AuthUser authUser, Long hospitalId, CheckInRequest checkInRequest){

        Integer currentCount = getCurrentCount();

        // 처리 중인 인원이 최대치보다 작고, 대기열이 없을 때 바로 통과
        if(currentCount<MAX_CONCURRENT_USERS && redisTemplate.opsForList().size(WAITING_QUEUE_KEY) == 0){
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

        List<String> queue = redisTemplate.opsForList().range(WAITING_QUEUE_KEY, 0, DUMPED_USERS_TO_SERVICE);

        // 현 작업자가 500명 미만이고 대기열에 사람이 있으면
        if(getCurrentCount()<DUMPED_USERS_TO_SERVICE && queue != null){
            // 대기열의 500번과 그 이상인 유저만 남기고 모두 제거
            redisTemplate.opsForList().trim(WAITING_QUEUE_KEY, DUMPED_USERS_TO_SERVICE, -1);
            // 대기열의 0~499번째 집어 넣기
            for(String userId : queue){
                // 저장해 놓은 요청을 가져와서
                String value = redisTemplate.opsForValue().get("check in request of user " + userId);

                int one = value.indexOf("일");
                int two = value.indexOf("이");
                int three = value.indexOf("삼");
                int four = value.indexOf("사");

                Long hospitalId = Long.valueOf(value.substring(0, one));
                Long doctorId = Long.valueOf(value.substring(one+1, two));
                String status = value.substring(two+1, three);
                CheckInRequest checkInRequest = new CheckInRequest(doctorId, status);

                Long authUserId = Long.valueOf(value.substring(three+1, four));
                Long patientId = Long.valueOf(value.substring(four+1));

                // 바로 집어넣기
                try {
                    checkInService.saveCheckIn(patientId, authUserId, hospitalId, checkInRequest);
                    notifyUser(userId, "접수 성공");
                } catch (ApiException e){
                    notifyUser(userId, e.getMessage());
                    throw e;
                }
            }
        }

        for(String userId : queue){
            int position = queue.indexOf(userId);
            redisTemplate.convertAndSend(
                    "/queue/position", userId + " " + position
            );
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

    // 대기가 끝났을 때 사용자에게 알림 전송
    private void notifyUser(String userId, String message) {
        // 웹소켓을 통해 사용자에게 알림
        redisTemplate.convertAndSend("/queue/status", userId + "번 유저 " + message);
    }

    // 사용자에게 대기열 순번 정보를 웹소켓으로 전송
    private void notifyUserPosition(String userId, int position) {
        redisTemplate.convertAndSend("/queue/position", userId + "번 유저의 대기 순번 : " + position);
    }

    // 대기열 내 위치 조회
    public int getPosition(String userId) {
        List<String> queue = redisTemplate.opsForList().range(WAITING_QUEUE_KEY, 0, -1);
        if (queue == null) return -1;
        return queue.indexOf(userId) + 1;
    }
}
