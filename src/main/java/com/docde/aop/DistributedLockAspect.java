package com.docde.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    public DistributedLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(distributedLock)")  // DistributedLock 어노테이션이 있는 메서드에만 적용
    public Object lockMethod(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = distributedLock.key();  // 어노테이션에 설정한 키 사용
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            // 지정한 대기 시간과 락 유지 시간을 사용해 락 획득 시도
            acquired = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), TimeUnit.SECONDS);
            if (acquired) {
                // 락을 획득하면 메서드 실행
                return joinPoint.proceed();
            } else {
                throw new RuntimeException("Unable to acquire lock for key: " + lockKey);
            }
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                // 락을 소유한 경우 락 해제
                lock.unlock();
            }
        }
    }
}
