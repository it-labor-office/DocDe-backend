package com.docde.config;

import com.docde.common.enums.UserRole;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = TestSecurityContextFactory.class)
public @interface WithMockAuthUser {
    long id() default 1L;

    String email() default "a@a.com";

    long doctorId() default 1L;

    long patientId() default 1L;

    long hospitalId() default 1L;

    UserRole userRole() default UserRole.ROLE_PATIENT;

}