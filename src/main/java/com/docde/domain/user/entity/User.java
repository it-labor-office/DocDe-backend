package com.docde.domain.user.entity;

import com.docde.common.entity.Timestamped;
import com.docde.common.enums.UserRole;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class User extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    UserRole userRole;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn
    Doctor doctor;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn
    Patient patient;

    @Builder
    public User(String email, String password, UserRole userRole, Doctor doctor, Patient patient) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.doctor = doctor;
        this.patient = patient;
    }
}
