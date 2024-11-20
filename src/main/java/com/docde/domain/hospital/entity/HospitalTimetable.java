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
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    public HospitalTimetable(DayOfTheWeek dayOfTheWeek, LocalTime openTime, LocalTime closeTime, Hospital hospital) {
        this.dayOfTheWeek = dayOfTheWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.hospital = hospital;
    }

    public void updateDayOfTheWeek(DayOfTheWeek dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public void updateOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public void updateCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }
}
