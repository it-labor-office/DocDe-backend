package com.docde.domain.auth.dto;

import com.docde.common.enums.UserRole;
import com.docde.domain.auth.dto.AuthResponse.DoctorSignUp;
import com.docde.domain.auth.dto.AuthResponse.PatientSignUp;
import com.docde.domain.auth.dto.AuthResponse.SignIn;
import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.patient.dto.PatientResponse;

public sealed interface AuthResponse permits PatientSignUp, DoctorSignUp, SignIn {
    record PatientSignUp(Long id, String email, UserRole userRole, PatientResponse patient) implements AuthResponse {
    }

    record DoctorSignUp(Long id, String email, UserRole userRole, DoctorResponse doctor) implements AuthResponse {
    }

    record SignIn(String accessToken, String refreshToken) implements AuthResponse {
    }
}
