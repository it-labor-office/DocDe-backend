package com.docde.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonConfig {

    public static RedissonClient createClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("${REDIS_LOCAL}"); // Redis 서버 주소 설정
        return Redisson.create(config);
    }
}