package com.docde.domain.user.entity;

import com.docde.common.entity.Timestamped;
import com.docde.common.enums.UserRole;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor
@SQLDelete(sql = "UPDATE user SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
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

    @Column(nullable = false)
    Boolean deleted;

    @Builder
    public User(String email, String password, UserRole userRole, Doctor doctor, Patient patient) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.doctor = doctor;
        this.patient = patient;
        this.deleted = false;
    }
}
