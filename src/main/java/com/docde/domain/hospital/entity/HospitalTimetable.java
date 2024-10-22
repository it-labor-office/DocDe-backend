package com.docde.domain.hospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Entity
@NoArgsConstructor
public class HospitalTimetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated
    private DayOfTheWeek dayOfTheWeek;
    //SUN,MON,TUE,WED,THU,FRI,SAT,HOLIDAY
    @Column(nullable = false)
    private LocalTime openTime;
    @Column(nullable = false)
    private LocalTime closeTime;

    @OneToOne
    @JoinColumn(name="hospital_id")
    private Hospital hospital;
}
