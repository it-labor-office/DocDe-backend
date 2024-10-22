package com.docde.domain.hospital.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class WeekTimetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name= "hospital_id")
    private Hospital hospital;

    @OneToMany(cascade = CascadeType.PERSIST,fetch = FetchType.LAZY,mappedBy = "weekTimetable")
    private List<HospitalTimetable> hospitalTimetables=new ArrayList<>();

    public WeekTimetable(Hospital hospital) {
        this.hospital=hospital;
    }

    public void setHospitalTimetableList(List<HospitalTimetable> hospitalTimetables) {
        this.hospitalTimetables=hospitalTimetables;
    }
}
