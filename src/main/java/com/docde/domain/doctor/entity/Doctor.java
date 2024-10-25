package com.docde.domain.doctor.entity;

import com.docde.common.entity.Timestamped;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Doctor extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "hospital_id")
    private Hospital hospital;

    @OneToMany(mappedBy = "doctor")
    private List<MedicalRecord> medicalRecords;

    @OneToOne(mappedBy = "doctor")
    private User user;

    @Builder
    public Doctor(String name, String description, Hospital hospital, User user) {
        this.name = name;
        this.description = description;
        this.hospital = hospital;
        this.user = user;
    }
}
