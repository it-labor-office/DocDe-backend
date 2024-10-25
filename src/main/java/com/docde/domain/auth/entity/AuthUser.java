package com.docde.domain.auth.entity;

import com.docde.common.enums.UserRole;
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

    @Builder
    public AuthUser(Long id, String email, UserRole userRole, Long doctorId, Long patientId, Long hospitalId) {
        this.id = id;
        this.email = email;
        this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.hospitalId = hospitalId;
    }
}
