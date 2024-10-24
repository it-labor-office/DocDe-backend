package com.docde.domain.hospital.entity;

import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalPutRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

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

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.PERSIST)
    private List<HospitalTimetable> timetables=new ArrayList<>();
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
    public void updateAll(HospitalPutRequestDto requestDto) {
        this.name=requestDto.getHospitalName();
        this.address=requestDto.getHospitalAddress();
        this.contact=requestDto.getHospitalContact();
        this.open_time=requestDto.getOpenTime();
        this.closing_time=requestDto.getClosingTime();
        this.announcement=requestDto.getAnnouncement();
    }

    public void updateTimetables(List<HospitalTimetable> timetables) {
        this.timetables=timetables;
    }
}
