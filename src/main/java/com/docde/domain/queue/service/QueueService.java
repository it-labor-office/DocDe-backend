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

    /* 반복 재시도
     반복 재시도를 바꿔서 n명씩 집어 넣는건?
     10초에 한번씩 현접속자를 확인한 후에 현접속자가 50명 이하로 떨어지면 대기열 0~49번을 집어넣기?(숫자는 예시)
     대기열 0~49번 이하 순서가 온 사람
     순서가 온 사람들 id를 집어넣고 접수 컨트롤러의 요청 실행시키기?
     그럼 리퀘스트바디는 어떻게 함? 이거 이쪽에서 저장해야 함?? 별 내용 없긴 한데...
     대기 시간을 다 보냈으면 대기 전에 사용자가 보냈던 요청 내용 그대로 실행됐으면 좋겠음
     그런데 지금 로직 대로면 대기중인 사람이 자리 났는지 확인하는 10초 이내에 자리 나면 광클하던 사람이 들어가는거 아닌가
     사용자 많으면 튕기고 큐에 들어가는건 ㅇㅋ
     큐에 사람이 하나라도 있으면 튕기고 큐에 들어가기 <-- 이거 추가해야 되는거 아님?
     ㅇㅋ 그렇게 했다 치고
     대기자가 하나라도 있는 상황에선 몇초에 한번 체크해서 몇자리 이상 있으면 몇명 밀어넣기 ㅇㅋ
     그러면 대기를 완료한 사람이 작업으로 들어가는건 어떻게...
     여기서 컨트롤러 메서드를 실행시켜야 함?
     그거 실행 시켜 봤자 대기열 있으면 막힘에 막혀서 다시 대기열로 올텐데
     '줄서있다가순서온사람' 이 들어갈 컨트롤러 메서드를 하나 더 만들면?
     그리고 그 메서드에 유저아이디 집어넣고 실행?
     근데 여기서 컨트롤러 레이어를 알면 안 되는 것 아닌가...
     GPT한테 물어보니 사용자(수동조작)나 프론트에 짬때리라는데 이게 맞나
     순서가 온 사람 아이디를 키로 해서 요청 내용을 레디스에 넣어 놓고 나중에 그걸 꺼낸다음
     여기서 바로 접수 서비스 메서드를 가지고 리턴 해도 되나?

     결론 구현할 로직 정리 :
     1. 컨트롤러에서 요청을 받을 때 지금 작업 중인 사람 수를 확인
     2. 특정 숫자 이상이면 에러 메시지를 보내고 그 유저 id를 키로 하는 요청 내용을 저장, 유저id를 레디스 대기열에 넣음
     3. n초에 한 번씩 실행되는 메서드에서 작업 중인 사람 수를 확인하고 일정 수치 이하로 떨어졌으면('대기열이 비어 있으면 작동안함)
     4. 대기열의 유저id를 정해진 수량만큼 꺼낸 후
     5. 해당 id를 키로 하는 요청 내용을 꺼내 접수 서비스 메서드의 파라미터로 집어 넣고 접수 저장을 함


     질문

     1. 이 클래스(서비스)에서 컨트롤러 메서드를 실행시켜도 되나요? 서비스 레이어에선 컨트롤러에 대해 몰라야 하니 안 되는게 맞나요?

     2. 여기서 접수 서비스 메서드를 실행시켜 접수를 저장해도 되나요? 그렇게 해도 된다면 결과는 웹소켓으로 알려도 되나요?

     3. 혹시 이런 문제는 클라이언트(프론트)가 처리하는 것이 '국룰' 인가요? 서버 쪽에서 처리해도 괜찮을까요?
     */


    @Scheduled(fixedRate = 10000)
    public void retry(){
        // 현 접속자가 50명 미만이면
        if(getCurrentCount()<50){
            // 대기열 0~49명 집어 넣기

        }
    }









    // 전에꺼
    @Scheduled(fixedRate = 10000)
    public void retryOld(){
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
