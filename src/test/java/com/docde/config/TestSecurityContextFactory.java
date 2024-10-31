package com.docde.config;

import com.docde.domain.auth.entity.AuthUser;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class TestSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthUser user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        AuthUser authUser = AuthUser.builder()
                .id(user.id())
                .userRole(user.userRole())
                .hospitalId(user.hospitalId())
                .email(user.email())
                .patientId(user.patientId())
                .doctorId(user.doctorId())
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(authUser);

        context.setAuthentication(authentication);
        return context;
    }
}