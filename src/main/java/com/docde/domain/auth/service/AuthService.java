package com.docde.domain.auth.service;

import com.docde.common.enums.Gender;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.InvalidRequestException;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.user.entity.User;
import com.docde.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public User patientSignUp(String email, String password, String name, String address, String phone, Gender gender) {
        if (userRepository.existsByEmail(email)) throw new InvalidRequestException("중복된 이메일입니다.");

        String encodedPassword = passwordEncoder.encode(password);
        Patient patient = Patient.builder().name(name).address(address).phone(phone).gender(gender).build();
        User user = User.builder().email(email).password(encodedPassword).userRole(UserRole.ROLE_PATIENT).patient(patient).build();
        return userRepository.save(user);
    }

    @Transactional
    public User doctorSignUp(String email, String password, String name, String description, Boolean isDoctorPresident) {
        if (userRepository.existsByEmail(email)) throw new InvalidRequestException("중복된 이메일입니다.");
        
        String encodedPassword = passwordEncoder.encode(password);
        Doctor doctor = Doctor.builder().name(name).description(description).build();
        User user = User.builder().email(email).password(encodedPassword).doctor(doctor).userRole(isDoctorPresident ? UserRole.ROLE_DOCTOR_PRESIDENT : UserRole.ROLE_DOCTOR).build();
        return userRepository.save(user);
    }
}
