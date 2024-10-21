package com.docde.common.enums;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum UserRole {
    ROLE_PATIENT(Authority.PATIENT),
    ROLE_DOCTOR_PRESIDENT(Authority.DOCTOR_PRESIDENT),
    ROLE_DOCTOR(Authority.DOCTOR);

    private final String userRole;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 UserRole"));
    }

    public static class Authority {
        public static final String PATIENT = "ROLE_PATIENT";
        public static final String DOCTOR = "ROLE_DOCTOR";
        public static final String DOCTOR_PRESIDENT = "ROLE_DOCTOR_PRESIDENT";
    }

}
