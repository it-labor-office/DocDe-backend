package com.docde.domain.reservation.queue;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueueMetricsService {
    private final Counter successCounter;
    private final Counter failureCounter;

    public QueueMetricsService(MeterRegistry meterRegistry) {
        this.successCounter = meterRegistry.counter("queue_requests_success_total");
        this.failureCounter = meterRegistry.counter("queue_requests_failure_total");
    }

    public void processQueueRequest(boolean success) {
        if (success) {
            successCounter.increment();
            log.info("성공한 총 요청 큐: {}", successCounter.count());

        } else {
            failureCounter.increment();
            log.info("실패한 총 요청 큐: {}", failureCounter.count());

        }
    }
}
