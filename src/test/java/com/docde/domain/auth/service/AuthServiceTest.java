package com.docde.domain.auth.service;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.enums.Gender;
import com.docde.common.enums.TokenType;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.config.JwtUtil;
import com.docde.domain.auth.dto.AuthResponse;
import com.docde.domain.user.entity.User;
import com.docde.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    AuthService authService;

    @Mock
    UserRepository userRepository;

    @Mock
    RedisTemplate redisTemplate;

    @Mock
    ValueOperations valueOperations;

    @Spy
    PasswordEncoder passwordEncoder;

    @Spy
    JwtUtil jwtUtil;

    @Mock
    JavaMailSender javaMailSender;

    @Nested
    @DisplayName("AuthService::_isValidPassword")
    class Test1 {

        @Test
        @DisplayName("최소 8자 이상이어야 하며, 대소문자 포함 영문, 숫자, 특수문자를 최소 1글자씩 포함하면 true.")
        void test1() {
            // given
            String password = "Password1234@";

            // when & then
            assertTrue(authService._isValidPassword(password));
        }

        @Test
        @DisplayName("최소 8자 이상이어야 하며, 대소문자 포함 영문, 숫자, 특수문자를 최소 1글자씩 포함 조건을 만족하지 못하면 false")
        void test2() {
            // given
            String password = "password1234@";

            // when & then
            assertFalse(authService._isValidPassword(password));
        }
    }


    @Nested
    @DisplayName("AuthService::patientSignUp")
    class Test2 {

        @Test
        @DisplayName("이메일 중복이면 예외가 발생한다.")
        void test1() {
            // given
            String email = "a@a.com";
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.patientSignUp(email, "", "", "", "", Gender.M, ""));
            assertEquals(apiException.getErrorCode(), ErrorStatus._DUPLICATED_EMAIL);
        }

        @Test
        @DisplayName("비밀번호가 유효하지 않으면 예외가 발생한다.")
        void test2() {
            // given
            String email = "a@a.com";
            String password = "password1234@";
            when(userRepository.existsByEmail(email)).thenReturn(false);

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.patientSignUp(email, password, "", "", "", Gender.M, ""));
            assertEquals(apiException.getErrorCode(), ErrorStatus._INVALID_PASSWORD_FORM);
        }

        @Test
        @DisplayName("전화번호가 유효하지 않으면 예외가 발생한다.")
        void test3() {
            // given
            String email = "a@a.com";
            String password = "Password1234@";
            String phone = "123";
            when(userRepository.existsByEmail(email)).thenReturn(false);

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.patientSignUp(email, password, "", "", phone, Gender.M, ""));
            assertEquals(apiException.getErrorCode(), ErrorStatus._INVALID_PHONE_FORM);
        }

        @Test
        @DisplayName("이메일 인증이 되지 않았으면 예외가 발생한다.")
        void test4() {
            // given
            String email = "a@a.com";
            String password = "Password1234@";
            String phone = "01012345678";
            String code = "qweqwe";
            String codeInRedis = "asdasd";
            String redisKey = String.format("%s_%s", "AUTHENTICATION_CODE_FOR_EMAIL", email);
            when(valueOperations.get(redisKey)).thenReturn(codeInRedis);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(userRepository.existsByEmail(email)).thenReturn(false);
            ReflectionTestUtils.setField(authService, "shouldAuthenticateEmail", "true");

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.patientSignUp(email, password, "", "", phone, Gender.M, code));
            assertEquals(apiException.getErrorCode(), ErrorStatus._EMAIL_MUST_BE_AUTHENTICATED);
        }

        @Test
        @DisplayName("환자와 유저가 잘 생성된다.")
        void test5() {
            // given
            String email = "a@a.com";
            String password = "Password1234@";
            String phone = "01012345678";
            String name = "이름";
            String address = "주소";
            Gender gender = Gender.M;
            String code = "qweqwe";
            when(userRepository.existsByEmail(email)).thenReturn(false);
            ReflectionTestUtils.setField(authService, "shouldAuthenticateEmail", "false");
            when(userRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

            // when
            User user = authService.patientSignUp(email, password, name, address, phone, gender, code);

            // then
            assertEquals(user.getUserRole(), UserRole.ROLE_PATIENT);
            assertEquals(user.getEmail(), email);
            assertEquals(user.getPatient().getAddress(), address);
            assertEquals(user.getPatient().getName(), name);
            assertEquals(user.getPatient().getPhone(), phone);
            assertEquals(user.getPatient().getGender(), gender);
        }
    }


    @Nested
    @DisplayName("AuthService::_isAuthenticatedEmail")
    class Test3 {
        @Test
        @DisplayName("SHOULD_AUTHENTICATE_EMAIL 이 \"false\" 또는 null 이면 이메일 인증 여부를 검사하지 않는다.")
        void test1() {
            // given
            ReflectionTestUtils.setField(authService, "shouldAuthenticateEmail", "false");

            // when & then
            assertTrue(authService._isAuthenticatedEmail("a@a.com", "qweqwe"));

            // given
            ReflectionTestUtils.setField(authService, "shouldAuthenticateEmail", null);

            // when & then
            assertTrue(authService._isAuthenticatedEmail("a@a.com", "qweqwe"));
        }

        @Test
        @DisplayName("SHOULD_AUTHENTICATE_EMAIL 이 true이면 이메일 인증 여부를 검사한다.")
        void test2() {
            // given
            ReflectionTestUtils.setField(authService, "shouldAuthenticateEmail", "true");
            String email = "a@a.com";
            String code = "qweqwe";
            String redisKey = String.format("%s_%s", "AUTHENTICATION_CODE_FOR_EMAIL", email);
            when(valueOperations.get(redisKey)).thenReturn(code);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // when & then
            assertTrue(authService._isAuthenticatedEmail(email, code));
        }

        @Test
        @DisplayName("인증 값이 없으면 false")
        void test3() {
            // given
            ReflectionTestUtils.setField(authService, "shouldAuthenticateEmail", "true");
            String email = "a@a.com";
            String code = "qweqwe";
            String redisKey = String.format("%s_%s", "AUTHENTICATION_CODE_FOR_EMAIL", email);
            when(valueOperations.get(redisKey)).thenReturn(null);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // when & then
            assertFalse(authService._isAuthenticatedEmail(email, code));
        }
    }

    @Nested
    @DisplayName("AuthService::doctorSignUp")
    class Test4 {
        @Test
        @DisplayName("이메일 중복이면 예외가 발생한다.")
        void test1() {
            // given
            String email = "a@a.com";
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.doctorSignUp(email, "", "", "", false, ""));
            assertEquals(apiException.getErrorCode(), ErrorStatus._DUPLICATED_EMAIL);
        }

        @Test
        @DisplayName("비밀번호가 유효하지 않으면 예외가 발생한다.")
        void test2() {
            // given
            String email = "a@a.com";
            String password = "password1234@";
            when(userRepository.existsByEmail(email)).thenReturn(false);

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.doctorSignUp(email, password, "", "", false, ""));
            assertEquals(apiException.getErrorCode(), ErrorStatus._INVALID_PASSWORD_FORM);
        }


        @Test
        @DisplayName("이메일 인증이 되지 않았으면 예외가 발생한다.")
        void test4() {
            // given
            String email = "a@a.com";
            String password = "Password1234@";
            String code = "qweqwe";
            String codeInRedis = "asdasd";
            String redisKey = String.format("%s_%s", "AUTHENTICATION_CODE_FOR_EMAIL", email);
            when(valueOperations.get(redisKey)).thenReturn(codeInRedis);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(userRepository.existsByEmail(email)).thenReturn(false);
            ReflectionTestUtils.setField(authService, "shouldAuthenticateEmail", "true");

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.doctorSignUp(email, password, "", "", false, code));
            assertEquals(apiException.getErrorCode(), ErrorStatus._EMAIL_MUST_BE_AUTHENTICATED);
        }

        @Test
        @DisplayName("의사와 유저가 잘 생성된다.")
        void test5() {
            // given
            String email = "a@a.com";
            String password = "Password1234@";
            String name = "이름";
            String description = "상세";
            String code = "qweqwe";
            when(userRepository.existsByEmail(email)).thenReturn(false);
            ReflectionTestUtils.setField(authService, "shouldAuthenticateEmail", "false");
            when(userRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

            // when
            User user = authService.doctorSignUp(email, password, name, description, false, code);

            // then
            assertEquals(user.getUserRole(), UserRole.ROLE_DOCTOR);
            assertEquals(user.getEmail(), email);
            assertEquals(user.getDoctor().getDescription(), description);
            assertEquals(user.getDoctor().getName(), name);

            // when
            user = authService.doctorSignUp(email, password, name, description, true, code);

            // then
            assertEquals(user.getUserRole(), UserRole.ROLE_DOCTOR_PRESIDENT);
            assertEquals(user.getEmail(), email);
            assertEquals(user.getDoctor().getDescription(), description);
            assertEquals(user.getDoctor().getName(), name);
        }
    }

    @Nested
    @DisplayName("AuthService::reissueToken")
    class Test5 {
        @Test
        @DisplayName("토큰이 null이거나 Bearer로 시작하지 않으면 예외 발생")
        void test1() {
            // given
            String token = null;

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.reissueToken(token));
            assertEquals(apiException.getErrorCode(), ErrorStatus._BAD_REQUEST_ILLEGAL_TOKEN);

            // given
            String token1 = "b";

            // when & then
            apiException = assertThrows(ApiException.class, () -> authService.reissueToken(token1));
            assertEquals(apiException.getErrorCode(), ErrorStatus._BAD_REQUEST_ILLEGAL_TOKEN);
        }

        @Test
        @DisplayName("토큰 타입이 리프레시가 아니면 예외 발생")
        void test2() {
            // given
            String token = "Bearer wowow";
            doReturn(TokenType.ACCESS).when(jwtUtil).getTokenType("wowow");

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.reissueToken(token));
            assertEquals(apiException.getErrorCode(), ErrorStatus._NOT_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("토큰이 만료되었으면 예외가 발생한다.")
        void test3() {
            // given
            String token = "Bearer wowow";
            String tokenWithoutBearer = "wowow";
            doReturn(TokenType.REFRESH).when(jwtUtil).getTokenType(tokenWithoutBearer);
            ExpiredJwtException expiredJwtException = mock(ExpiredJwtException.class);
            doThrow(expiredJwtException).when(jwtUtil).isExpired(tokenWithoutBearer);

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.reissueToken(token));
            assertEquals(apiException.getErrorCode(), ErrorStatus._UNAUTHORIZED_EXPIRED_TOKEN);

            // given
            doReturn(true).when(jwtUtil).isExpired(tokenWithoutBearer);

            // when & then
            apiException = assertThrows(ApiException.class, () -> authService.reissueToken(token));
            assertEquals(apiException.getErrorCode(), ErrorStatus._UNAUTHORIZED_EXPIRED_TOKEN);
        }

        @Test
        @DisplayName("토큰을 새로 발급받는다.")
        void test4() {
            // given
            String idStr = "1";
            Long userId = Long.parseLong(idStr);
            String email = "a@a.com";
            UserRole userRole = UserRole.ROLE_PATIENT;
            String token = "Bearer wowow";
            String tokenWithoutBearer = "wowow";
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";

            Claims claims = mock(Claims.class);
            when(claims.getSubject()).thenReturn(idStr);
            when(claims.get("email", String.class)).thenReturn(email);
            when(claims.get("userRole", UserRole.class)).thenReturn(UserRole.ROLE_PATIENT);

            doReturn(TokenType.REFRESH).when(jwtUtil).getTokenType(tokenWithoutBearer);
            doReturn(claims).when(jwtUtil).extractClaims(tokenWithoutBearer);
            doReturn(accessToken).when(jwtUtil).createAccessToken(userId, email, userRole);
            doReturn(refreshToken).when(jwtUtil).createRefreshToken(userId, email, userRole);
            doReturn(false).when(jwtUtil).isExpired(tokenWithoutBearer);

            // when
            AuthResponse.SignIn signIn = authService.reissueToken(token);

            // then
            assertEquals(signIn.accessToken(), accessToken);
            assertEquals(signIn.refreshToken(), refreshToken);
        }
    }

    @Nested
    @DisplayName("AuthService::_createAuthenticationCode")
    class Test6 {
        @DisplayName("코드가 랜덤하게 뽑힌다")
        @RepeatedTest(value = 50, name = "{displayName}, {currentRepetition}/{totalRepetitions}")
        void test1() {
            String code = authService._createAuthenticationCode();
            assertEquals(code.length(), 8);
        }
    }

    @Nested
    @DisplayName("AuthService::authenticateEmail")
    class Test7 {
        @Test
        @DisplayName("이메일이 정상적으로 보내진다.")
        void test1() {
            // given
            MimeMessage mimeMessage = mock(MimeMessage.class);
            String email = "a@a.com";
            String googleAccountEmail = "b@gmail.com";
            ReflectionTestUtils.setField(authService, "googleAccountEmail", googleAccountEmail);
            when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // when & then
            assertDoesNotThrow(() -> authService.authenticateEmail(email));
        }

        @Test
        @DisplayName("이메일이 정상적으로 보내지지 않으면 예외 발생.")
        void test2() {
            // given
            MimeMessage mimeMessage = mock(MimeMessage.class);
            String email = "a@a.com";
            String googleAccountEmail = "b@gmail.com";
            ReflectionTestUtils.setField(authService, "googleAccountEmail", googleAccountEmail);
            when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            MailException mailException = mock(MailException.class);
            doThrow(mailException).when(javaMailSender).send(mimeMessage);

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> authService.authenticateEmail(email));
            assertEquals(apiException.getErrorCode(), ErrorStatus._ERROR_WHILE_SENDING_EMAIL);
        }
    }
}
