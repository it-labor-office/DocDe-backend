package com.docde.domain.reservation.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationQueueManager {

    private final ReservationQueueConsumer reservationQueueConsumer;

    public void triggerQueueProcessor(RedisQueueService redisQueueService) {
        reservationQueueConsumer.startQueueProcessor(redisQueueService);
    }
}