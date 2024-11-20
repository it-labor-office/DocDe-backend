package com.docde.common.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.docde.domain.queue.service.QueueService.CURRENT_COUNT_KEY;

@Aspect
@Component
@RequiredArgsConstructor
public class CurrentCountAspect {

    private final RedisTemplate<String, String> redisTemplate;

    @Around(value = "@annotation(CurrentCount)")
    public Object CountUser(ProceedingJoinPoint joinPoint) throws Throwable {
        incrementCurrentCount();
        try {
            return joinPoint.proceed();
        } finally {
            decrementCurrentCount();
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
}
