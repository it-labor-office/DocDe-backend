package com.docde.domain.auth.dto;

import com.docde.common.enums.Gender;
import com.docde.domain.auth.dto.AuthRequest.DoctorSignUp;
import com.docde.domain.auth.dto.AuthRequest.PatientSignUp;
import com.docde.domain.auth.dto.AuthRequest.SignIn;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public sealed interface AuthRequest permits PatientSignUp, DoctorSignUp, SignIn {
    record PatientSignUp(@NotBlank @Email String email, @NotBlank String password, @NotBlank String name,
                         @NotBlank String address, @NotBlank String phone,
                         @NotNull Gender gender) implements AuthRequest {
    }

    record DoctorSignUp(@NotBlank @Email String email, @NotBlank String password, @NotBlank String name,
                        @NotBlank String description, @NotNull Boolean isDoctorPresident) implements AuthRequest {
    }

    record SignIn(@NotBlank @Email String email, @NotBlank String password) implements AuthRequest {
    }
}
