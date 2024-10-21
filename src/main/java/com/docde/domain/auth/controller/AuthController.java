package com.docde.domain.auth.controller;

import com.docde.domain.auth.dto.AuthRequest;
import com.docde.domain.auth.dto.AuthResponse;
import com.docde.domain.auth.service.AuthService;
import com.docde.domain.doctor.dto.DoctorResponse;
import com.docde.domain.patient.dto.PatientResponse;
import com.docde.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/docde/auth/signup")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/patient")
    public ResponseEntity<AuthResponse.PatientSignUp> patientSignUp(@RequestBody @Valid AuthRequest.PatientSignUp signUpRequestDto) {
        User user = authService.patientSignUp(signUpRequestDto.email(), signUpRequestDto.password(), signUpRequestDto.name(), signUpRequestDto.address(), signUpRequestDto.phone(), signUpRequestDto.gender());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse.PatientSignUp(user.getId(), user.getEmail(), user.getUserRole(), new PatientResponse(user.getPatient())));
    }

    @PostMapping("/doctor")
    public ResponseEntity<AuthResponse.DoctorSignUp> doctorSignUp(@RequestBody @Valid AuthRequest.DoctorSignUp signUpRequestDto) {
        User user = authService.doctorSignUp(signUpRequestDto.email(), signUpRequestDto.password(), signUpRequestDto.name(), signUpRequestDto.description(), signUpRequestDto.isDoctorPresident());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse.DoctorSignUp(user.getId(), user.getEmail(), user.getUserRole(), new DoctorResponse(user.getDoctor())));
    }
}
