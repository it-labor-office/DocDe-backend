package com.docde.domain.hospital.entity;

import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Getter
@Entity
@NoArgsConstructor
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private LocalTime open_time;

    @Column(nullable = false)
    private LocalTime closing_time;

    @OneToOne(cascade = CascadeType.ALL)
    private WeekTimetable weekTimetable;
//    //병원은 승인을 받아야 한다.
//    private boolean isApprove=false;

    @Column(nullable = false)
    private String announcement;

    public Hospital(HospitalPostRequestDto requestDto) {
        this.name=requestDto.getHospitalName();
        this.address=requestDto.getHospitalAddress();
        this.contact=requestDto.getHospitalContact();
        this.open_time=requestDto.getOpenTime();
        this.closing_time=requestDto.getClosingTime();
        this.announcement=requestDto.getAnnouncement();
    }

    public void addWeekTimetable(WeekTimetable weekTimetable) {
        this.weekTimetable = weekTimetable;
    }
}
