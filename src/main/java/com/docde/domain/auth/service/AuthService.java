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
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final JavaMailSender javaMailSender;
    private final RedisTemplate redisTemplate;

    @Value("${GOOGLE_ACCOUNT_EMAIL}")
    private String googleAccountEmail;

    @Value("${SHOULD_AUTHENTICATE_EMAIL}")
    private String shouldAuthenticateEmail;

    private static final String AUTHENTICATION_CODE_FOR_EMAIL_REDIS_KEY = "AUTHENTICATION_CODE_FOR_EMAIL";

    public boolean _isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$");
    }

    @Transactional
    public User patientSignUp(String email, String password, String name, String address, String phone, Gender gender, String code) {
        if (userRepository.existsByEmail(email)) throw new ApiException(ErrorStatus._DUPLICATED_EMAIL);
        if (!_isValidPassword(password)) throw new ApiException(ErrorStatus._INVALID_PASSWORD_FORM);
        if (!phone.matches("^[0-9]{11}$")) throw new ApiException(ErrorStatus._INVALID_PHONE_FORM);
        if (!_isAuthenticatedEmail(email, code)) throw new ApiException(ErrorStatus._EMAIL_MUST_BE_AUTHENTICATED);

        String encodedPassword = passwordEncoder.encode(password);
        Patient patient = Patient.builder().name(name).address(address).phone(phone).gender(gender).build();
        User user = User.builder().email(email).password(encodedPassword).userRole(UserRole.ROLE_PATIENT).patient(patient).build();
        return userRepository.save(user);
    }

    @Transactional
    public User doctorSignUp(String email, String password, String name, String description, Boolean isDoctorPresident, String code) {
        if (userRepository.existsByEmail(email)) throw new ApiException(ErrorStatus._DUPLICATED_EMAIL);
        if (!_isValidPassword(password)) throw new ApiException(ErrorStatus._INVALID_PASSWORD_FORM);
        if (!_isAuthenticatedEmail(email, code)) throw new ApiException(ErrorStatus._EMAIL_MUST_BE_AUTHENTICATED);

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
        } catch (NullPointerException | IllegalArgumentException e) {
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

    public boolean _isAuthenticatedEmail(String email, String code) {
        if (shouldAuthenticateEmail == null || shouldAuthenticateEmail.equalsIgnoreCase("false")) return true;
        String redisKey = String.format("%s_%s", AUTHENTICATION_CODE_FOR_EMAIL_REDIS_KEY, email);
        String codeInRedis = (String) redisTemplate.opsForValue().get(redisKey);
        if (codeInRedis == null) return false;
        return codeInRedis.equals(code);
    }

    // 인증 코드 생성 메서드
    public String _createAuthenticationCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) { // 총 8자리 인증 번호 생성
            int idx = random.nextInt(3); // 0~2 사이의 값을 랜덤하게 받아와 idx에 집어넣습니다

            // 0,1,2 값을 switchcase를 통해 꼬아버립니다.
            // 숫자와 ASCII 코드를 이용합니다.
            switch (idx) {
                case 0:
                    // 0일 때, a~z 까지 랜덤 생성 후 key에 추가
                    key.append((char) (random.nextInt(26) + 97));
                    break;
                case 1:
                    // 1일 때, A~Z 까지 랜덤 생성 후 key에 추가
                    key.append((char) (random.nextInt(26) + 65));
                    break;
                case 2:
                    // 2일 때, 0~9 까지 랜덤 생성 후 key에 추가
                    key.append(random.nextInt(9));
                    break;
            }
        }
        return key.toString();
    }

    @Transactional
    public void authenticateEmail(String email) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true); // Helper 사용
            messageHelper.setFrom(googleAccountEmail);
            messageHelper.setTo(email);
            messageHelper.setSubject("[의사결정] 이메일 인증 번호 발송");
            String code = _createAuthenticationCode();

            String body = "<html>" +
                    "<body>" +
                    "아래 코드로 인증을 완료해주세요.<br/>" +
                    "<h1>" + code + "</h1>" +
                    "</body>" +
                    "</html>";

            messageHelper.setText(body, true);
            javaMailSender.send(mimeMessage);

            String redisKey = String.format("%s_%s", AUTHENTICATION_CODE_FOR_EMAIL_REDIS_KEY, email);
            redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES); // 5분동안 유효한 코드
        } catch (Exception e) {
            throw new ApiException(ErrorStatus._ERROR_WHILE_SENDING_EMAIL, e);
        }
    }

    @Transactional
    public AuthResponse.SignIn signIn(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException(ErrorStatus._EMAIL_OR_PASSWORD_NOT_MATCHES));
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new ApiException(ErrorStatus._EMAIL_OR_PASSWORD_NOT_MATCHES);

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), user.getUserRole());
        return new AuthResponse.SignIn(accessToken, refreshToken);
    }
}
