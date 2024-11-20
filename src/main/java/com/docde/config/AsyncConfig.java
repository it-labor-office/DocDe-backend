package com.docde.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(90);
        executor.setMaxPoolSize(150);
        executor.setQueueCapacity(15000); // 처리 대기 큐 용량
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.setAllowCoreThreadTimeOut(false); // Core 스레드 타임아웃 방지
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 큐가 가득 찬 경우 요청을 거부하지 않도록 처리

        executor.initialize();
        return executor;
    }
}