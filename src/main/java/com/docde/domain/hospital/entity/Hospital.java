package com.docde.domain.hospital.entity;

import com.docde.common.entity.Timestamped;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.hospital.dto.request.HospitalPostRequestDto;
import com.docde.domain.hospital.dto.request.HospitalUpdateRequestDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(indexes = @Index(name = "idx_name", columnList = "name"))
public class Hospital extends Timestamped {
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
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closingTime;

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "hospital", fetch = FetchType.LAZY)
    private List<Doctor> doctors;

    @Column(nullable = false)
    private String announcement;

    public void delete() {
        deleted = true;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateContact(String contact) {
        this.contact = contact;
    }

    public void updateOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public void updateClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public void updateAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public Hospital(String name, String address, LocalTime openTime, LocalTime closingTime, String contact, String announcement) {
        this.name = name;
        this.address = address;
        this.openTime = openTime;
        this.contact = contact;
        this.closingTime = closingTime;
        this.announcement = announcement;
        this.doctors = List.of();
    }

    public Hospital(HospitalPostRequestDto requestDto) {
        this.name = requestDto.getHospitalName();
        this.address = requestDto.getHospitalAddress();
        this.contact = requestDto.getHospitalContact();
        this.openTime = requestDto.getOpenTime();
        this.closingTime = requestDto.getClosingTime();
        this.announcement = requestDto.getAnnouncement();
        this.doctors = List.of();
    }

    public void updateAll(HospitalUpdateRequestDto requestDto) {
        this.name = requestDto.getHospitalName();
        this.address = requestDto.getHospitalAddress();
        this.contact = requestDto.getHospitalContact();
        this.openTime = requestDto.getOpenTime();
        this.closingTime = requestDto.getClosingTime();
        this.announcement = requestDto.getAnnouncement();
        this.doctors = List.of();
    }

    @Builder
    public Hospital(String name, String address, String contact, LocalTime openTime, LocalTime closingTime, String announcement) {
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.openTime = openTime;
        this.closingTime = closingTime;
        this.deleted = false;
        this.doctors = List.of();
        this.announcement = announcement;
    }
}
