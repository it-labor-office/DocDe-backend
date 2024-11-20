package com.docde.domain.reservation.cache;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheData(String key, Object value, long ttlInMinutes) {
        // Boolean 값을 String으로 변환하여 저장
        String serializedValue = (value instanceof Boolean) ? value.toString() : (String) value;
        redisTemplate.opsForValue().set(key, serializedValue, Duration.ofMinutes(ttlInMinutes));
    }

    // REDIS에서 특정 키 저장된 캐시 데이터를 가져옴
    public Object getCachedData(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}