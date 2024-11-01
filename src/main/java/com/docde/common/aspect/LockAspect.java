package com.docde.common.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Aspect
@Component
public class LockAspect {

    private final RedissonClient redissonClient; // redisson 라이브러리
    private static final String LOCK_KEY_PREFIX = "distributed-counter-lock:";
    // Redis에 저장될 락 키 접두어, 특정 락을 식별하기 위한 문자열


    public LockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }


    @Around("@annotation(Lockable) && args(key)")
    public Object around(ProceedingJoinPoint joinPoint, Long key) throws Throwable {

        String lockKey = LOCK_KEY_PREFIX + key; // 락을 식별할 키 생성
        RLock lock = redissonClient.getFairLock(lockKey); // Redis 에서 공정락을 가져옴(락 요청 순서대로 락 부여)

        // 10초 내로 락 획득 시도
        if (lock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                return joinPoint.proceed(); // 원래 호출된 메서드 실행
            } finally {
                lock.unlock(); // 작업 완료 후 락 해제
            }
        } else {
            throw new IllegalStateException("락 획득 실패: " + lockKey);
        }
    }
}
