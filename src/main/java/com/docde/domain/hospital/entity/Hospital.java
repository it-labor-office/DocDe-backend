package com.docde.domain.hospital.entity;

import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalUpdateRequestDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

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

//    @OneToMany(mappedBy = "hospital", cascade = CascadeType.PERSIST)
//    private List<HospitalTimetable> timetables = new ArrayList<>();
//    //병원은 승인을 받아야 한다.
//    private boolean isApprove=false;

    @Column(nullable = false)
    private String announcement;

    public void updateName(String name) {
        this.name = name;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateContact(String contact) {
        this.contact = contact;
    }

    public void updateOpenTime(LocalTime open_time) {
        this.open_time = open_time;
    }

    public void updateClosingTime(LocalTime closing_time) {
        this.closing_time = closing_time;
    }

    public void updateAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public Hospital(String name, String address, LocalTime open_time, LocalTime closing_time, String contact, String announcement) {
        this.name = name;
        this.address = address;
        this.open_time = open_time;
        this.contact = contact;
        this.closing_time = closing_time;
        this.announcement = announcement;
    }

    public Hospital(HospitalPostRequestDto requestDto) {
        this.name = requestDto.getHospitalName();
        this.address = requestDto.getHospitalAddress();
        this.contact = requestDto.getHospitalContact();
        this.open_time = requestDto.getOpenTime();
        this.closing_time = requestDto.getClosingTime();
        this.announcement = requestDto.getAnnouncement();
    }

    public void updateAll(HospitalUpdateRequestDto requestDto) {
        this.name = requestDto.getHospitalName();
        this.address = requestDto.getHospitalAddress();
        this.contact = requestDto.getHospitalContact();
        this.open_time = requestDto.getOpenTime();
        this.closing_time = requestDto.getClosingTime();
        this.announcement = requestDto.getAnnouncement();
    }

    @Builder
    public Hospital(String name, String address, String contact, LocalTime open_time, LocalTime closing_time, String announcement) {
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.open_time = open_time;
        this.closing_time = closing_time;
        this.announcement = announcement;
    }
//    public void updateTimetables(List<HospitalTimetable> timetables) {
//        this.timetables = timetables;
//    }
}
