package com.docde.domain.doctor.entity;

import com.docde.common.entity.Timestamped;
import com.docde.domain.hospital.entity.Hospital;
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
@SQLDelete(sql = "UPDATE doctor SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Doctor extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String medicalDepartment;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn
    private Hospital hospital;

    @OneToMany(mappedBy = "doctor")
    private List<MedicalRecord> medicalRecords;

    @OneToOne(mappedBy = "doctor", fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private Boolean deleted;

    @Builder
    public Doctor(String name, String medicalDepartment, Hospital hospital, User user) {
        this.name = name;
        this.medicalDepartment = medicalDepartment;
        this.hospital = hospital;
        this.user = user;
        this.deleted = false;
        this.medicalRecords = List.of();
    }

    //병원정보 업데이트하는 용도의 메서드
    public void addDoctorToHospital(Hospital hospital) {
        this.hospital = hospital;
    }
}
