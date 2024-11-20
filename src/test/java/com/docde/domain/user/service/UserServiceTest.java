package com.docde.domain.user.service;

import com.docde.common.response.ErrorStatus;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.user.entity.User;
import com.docde.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("UserService::deleteUser")
    class Test1 {
        @Test
        @DisplayName("찾는 유저가 없으면 예외가 발생한다.")
        void test1() {
            // given
            Long userId = 1L;
            String password = "password1234";
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> userService.deleteUser(userId, password));
            assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN);
        }

        @Test
        @DisplayName("입력된 비밀번호와 유저의 비밀번호가 다르면 예외가 발생한다.")
        void test2() {
            // given
            Long userId = 1L;
            String password = "password1234";
            String encodePassword = "diff";
            User user = User.builder().password(encodePassword).build();
            ReflectionTestUtils.setField(user, "id", userId);
            when(passwordEncoder.matches(password, encodePassword)).thenReturn(false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when & then
            ApiException apiException = assertThrows(ApiException.class, () -> userService.deleteUser(userId, password));
            assertEquals(apiException.getErrorCode(), ErrorStatus._FORBIDDEN);
        }

        @Test
        @DisplayName("유저 삭제가 정상적으로 작동한다.")
        void test3() {
            // given
            Long userId = 1L;
            String password = "password1234";
            String encodePassword = "password1234";
            User user = User.builder().password(encodePassword).build();
            ReflectionTestUtils.setField(user, "id", userId);
            when(passwordEncoder.matches(password, encodePassword)).thenReturn(true);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when & then
            assertDoesNotThrow(() -> userService.deleteUser(userId, password));
        }
    }

    @Nested
    @DisplayName("UserService::hasPermissionToDeleteUser")
    class Test2 {
        @Test
        @DisplayName("로그인한 유저가 삭제하려는 유저와 다른 유저면 false")
        void test1() {
            // given
            Long authUserId = 1L;
            Long userId = 2L;
            AuthUser authUser = AuthUser.builder().id(authUserId).userRole(UserRole.ROLE_PATIENT).build();

            // when & then
            assertFalse(userService.hasPermissionToDeleteUser(userId, authUser));
        }

        @Test
        @DisplayName("로그인한 유저가 삭제하려는 유저와 같은 유저면 true")
        void test2() {
            // given
            Long authUserId = 1L;
            Long userId = 1L;
            AuthUser authUser = AuthUser.builder().id(authUserId).userRole(UserRole.ROLE_PATIENT).build();

            // when & then
            assertTrue(userService.hasPermissionToDeleteUser(userId, authUser));
        }
    }
}
