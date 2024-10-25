package com.docde.domain.patient.entity;

import com.docde.common.entity.Timestamped;
import com.docde.common.enums.Gender;
import com.docde.domain.medicalRecord.entity.MedicalRecord;
import com.docde.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@SQLDelete(sql = "UPDATE patient SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
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

    @OneToOne(mappedBy = "patient")
    private User user;

    @Column(nullable = false)
    Boolean deleted;

    @Builder
    public Patient(String name, String address, String phone, Gender gender, User user) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.gender = gender;
        this.user = user;
        this.deleted = false;
    }
}
