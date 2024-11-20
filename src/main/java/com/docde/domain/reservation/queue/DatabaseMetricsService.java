package com.docde.domain.reservation.queue;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class DatabaseMetricsService {
    private final Counter dbSuccessCounter;
    private final Counter dbFailureCounter;

    public DatabaseMetricsService(MeterRegistry meterRegistry) {
        this.dbSuccessCounter = meterRegistry.counter("db_requests_success_total");
        this.dbFailureCounter = meterRegistry.counter("db_requests_failure_total");
    }

    public void recordDatabaseRequest(boolean success) {
        if (success) {
            dbSuccessCounter.increment();
        } else {
            dbFailureCounter.increment();
        }
    }
}
