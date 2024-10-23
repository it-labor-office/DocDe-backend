package com.docde.domain.auth.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.enums.Gender;
import com.docde.common.enums.TokenType;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.config.JwtUtil;
import com.docde.domain.auth.dto.AuthResponse;
import com.docde.domain.doctor.entity.Doctor;
import com.docde.domain.patient.entity.Patient;
import com.docde.domain.user.entity.User;
import com.docde.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$");
    }

    @Transactional
    public User patientSignUp(String email, String password, String name, String address, String phone, Gender gender) {
        if (userRepository.existsByEmail(email)) throw new ApiException(ErrorStatus._DUPLICATED_EMAIL);
        if (!isValidPassword(password))
            throw new ApiException(ErrorStatus._INVALID_PASSWORD_FORM);
        if (!phone.matches("^[0-9]{11}$"))
            throw new ApiException(ErrorStatus._INVALID_PHONE_FORM);

        String encodedPassword = passwordEncoder.encode(password);
        Patient patient = Patient.builder().name(name).address(address).phone(phone).gender(gender).build();
        User user = User.builder().email(email).password(encodedPassword).userRole(UserRole.ROLE_PATIENT).patient(patient).build();
        return userRepository.save(user);
    }

    @Transactional
    public User doctorSignUp(String email, String password, String name, String description, Boolean isDoctorPresident) {
        if (userRepository.existsByEmail(email)) throw new ApiException(ErrorStatus._DUPLICATED_EMAIL);
        if (!isValidPassword(password))
            throw new ApiException(ErrorStatus._INVALID_PASSWORD_FORM);

        String encodedPassword = passwordEncoder.encode(password);
        Doctor doctor = Doctor.builder().name(name).description(description).build();
        User user = User.builder().email(email).password(encodedPassword).doctor(doctor).userRole(isDoctorPresident ? UserRole.ROLE_DOCTOR_PRESIDENT : UserRole.ROLE_DOCTOR).build();
        return userRepository.save(user);
    }

    @Transactional
    public AuthResponse.SignIn reissueToken(String refreshToken) {
        // 프론트에서 붙여준 Bearer prefix 제거
        try {
            refreshToken = jwtUtil.substringToken(refreshToken);
        } catch (NullPointerException e) {
            throw new ApiException(ErrorStatus._BAD_REQUEST_ILLEGAL_TOKEN);
        }

        // 리프레쉬 토큰인지 검사
        TokenType tokenType = jwtUtil.getTokenType(refreshToken);
        if (!tokenType.equals(TokenType.REFRESH))
            throw new ApiException(ErrorStatus._NOT_REFRESH_TOKEN);

        // 토큰 만료 검사
        try {
            if (jwtUtil.isExpired(refreshToken))
                throw new ApiException(ErrorStatus._UNAUTHORIZED_EXPIRED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorStatus._UNAUTHORIZED_EXPIRED_TOKEN);
        }

        Claims claims = jwtUtil.extractClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = claims.get("userRole", UserRole.class);

        // 새 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(userId, email, userRole);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, email, userRole);
        return new AuthResponse.SignIn(newAccessToken, newRefreshToken);
    }
}
