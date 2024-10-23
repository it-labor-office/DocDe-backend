package com.docde.domain.patient.entity;

import com.docde.common.entity.Timestamped;
import com.docde.common.enums.Gender;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Patient extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String address;

    @Column(nullable = false)
    String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Gender gender;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<MedicalRecord> medicalRecord;

    @Builder
    public Patient(String name, String address, String phone, Gender gender) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.gender = gender;
    }
}
