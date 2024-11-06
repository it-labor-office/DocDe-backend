/*
package com.docde.distributedlock;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class DistributedCounter {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int count;

    @Column(nullable = false, unique = true)
    private Long doctorId;

    @Column(nullable = false)
    private LocalDateTime reservationTime; // 예약 시간

    public DistributedCounter(Long doctorId, int count, LocalDateTime reservationTime) {
        this.doctorId = doctorId;
        this.count = count;
        this.reservationTime = reservationTime;
    }

    public DistributedCounter increment() {
        this.count++; // 카운트 증가 및 자신 참조 반환
        return this;
    }
}*/
