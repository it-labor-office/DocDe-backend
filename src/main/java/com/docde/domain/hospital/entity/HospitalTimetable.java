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

    @Enumerated(EnumType.STRING)
    private DayOfTheWeek dayOfTheWeek;
    //SUN,MON,TUE,WED,THU,FRI,SAT,HOLIDAY
    @Column(nullable = false)
    private LocalTime openTime;
    @Column(nullable = false)
    private LocalTime closeTime;
    @ManyToOne
    @JoinColumn(name="week_timetable_id")
    private WeekTimetable weekTimetable;

    public HospitalTimetable(DayOfTheWeek dayOfTheWeek, LocalTime openTime, LocalTime closeTime, WeekTimetable weekTimetable) {
        this.dayOfTheWeek = dayOfTheWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.weekTimetable = weekTimetable;
    }
}
