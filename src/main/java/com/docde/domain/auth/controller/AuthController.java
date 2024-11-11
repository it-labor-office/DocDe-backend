package com.docde.domain.auth.controller;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.domain.auth.dto.AuthRequest;
import com.docde.domain.auth.dto.AuthResponse;
import com.docde.domain.auth.service.AuthService;
import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.patient.dto.PatientResponse;
import com.docde.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/signup/patient")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse.PatientSignUp> patientSignUp(@RequestBody @Valid AuthRequest.PatientSignUp signUpRequestDto) {
        User user = authService.patientSignUp(signUpRequestDto.email(), signUpRequestDto.password(), signUpRequestDto.name(), signUpRequestDto.address(), signUpRequestDto.phone(), signUpRequestDto.gender(), signUpRequestDto.code());
        return ApiResponse.onCreated(new AuthResponse.PatientSignUp(user.getId(), user.getEmail(), user.getUserRole(), new PatientResponse(user.getPatient())));
    }

    @PostMapping("/auth/signup/doctor")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse.DoctorSignUp> doctorSignUp(@RequestBody @Valid AuthRequest.DoctorSignUp signUpRequestDto) {
        User user = authService.doctorSignUp(signUpRequestDto.email(), signUpRequestDto.password(), signUpRequestDto.name(), signUpRequestDto.medicalDepartment(), signUpRequestDto.isDoctorPresident(), signUpRequestDto.code());
        return ApiResponse.onCreated(new AuthResponse.DoctorSignUp(user.getId(), user.getEmail(), user.getUserRole(), new DoctorResponse(user.getDoctor())));
    }

    @PostMapping("/auth/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse.SignIn> reissueToken(@RequestBody @Valid AuthRequest.ReissueToken reissueTokenRequestDto) {
        return ApiResponse.onCreated(authService.reissueToken(reissueTokenRequestDto.refreshToken()));
    }

    @PostMapping("/auth/email-authentication")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse authenticateEmail(@RequestBody @Valid AuthRequest.AuthenticateEmail authenticateEmailRequestDto) {
        authService.authenticateEmail(authenticateEmailRequestDto.email());
        return ApiResponse.onCreated(null);
    }

    @PostMapping("/auth/signin")
    public ApiResponse<AuthResponse.SignIn> signIn(@RequestBody @Valid AuthRequest.SignIn signInRequestDto) {
        return ApiResponse.onCreated(authService.signIn(signInRequestDto.email(), signInRequestDto.password()));
    }
}
