package com.docde.domain.auth.dto;

import com.docde.common.enums.Gender;
import com.docde.domain.auth.dto.AuthRequest.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public sealed interface AuthRequest permits PatientSignUp, DoctorSignUp, SignIn, ReissueToken, AuthenticateEmail {
    record PatientSignUp(@NotBlank @Email String email, @NotBlank String password, @NotBlank String name,
                         @NotBlank String address, @NotBlank String phone,
                         @NotNull Gender gender, @NotBlank String code) implements AuthRequest {
    }

    record DoctorSignUp(@NotBlank @Email String email, @NotBlank String password, @NotBlank String name,
                        @NotBlank String medicalDepartment, @NotNull Boolean isDoctorPresident,
                        @NotBlank String code) implements AuthRequest {
    }

    record SignIn(@NotBlank @Email String email, @NotBlank String password) implements AuthRequest {
    }

    record ReissueToken(@NotBlank String refreshToken) implements AuthRequest {
    }

    record AuthenticateEmail(@NotBlank @Email String email) implements AuthRequest {
    }
}
