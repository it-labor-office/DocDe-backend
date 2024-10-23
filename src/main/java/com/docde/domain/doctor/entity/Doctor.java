package com.docde.domain.doctor.entity;

import com.docde.domain.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "hospital_id")
    private Hospital hospital;

    @Builder
    public Doctor(String name, String description, Hospital hospital) {
        this.name = name;
        this.description = description;
        this.hospital = hospital;
    }
}
