package com.docde.domain.auth.entity;

import com.docde.common.enums.UserRole;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.hospital.entity.Hospital;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {
    private final Long id;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Long doctorId;
    private final Long patientId;
    private final Long hospitalId;
    private final UserRole userRole;

    @Builder
    public AuthUser(Long id, String email, UserRole userRole, Long doctorId, Long patientId, Long hospitalId) {
        this.id = id;
        this.email = email;
        this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.hospitalId = hospitalId;
        this.userRole = userRole;
    }

    public AuthUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.authorities = List.of(new SimpleGrantedAuthority(user.getUserRole().name()));
        Doctor doctor = user.getDoctor();
        Patient patient = user.getPatient();
        Hospital hospital = doctor == null ? null : doctor.getHospital();
        this.doctorId = doctor == null ? null : doctor.getId();
        this.patientId = patient == null ? null : patient.getId();
        this.hospitalId = hospital == null ? null : hospital.getId();
        this.userRole = user.getUserRole();
    }
}
