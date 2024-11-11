package com.docde.domain.user.controller;

import com.docde.common.enums.UserRole;
import com.docde.config.JwtAuthenticationToken;
import com.docde.config.JwtUtil;
import com.docde.config.SecurityConfig;
import com.docde.config.WithMockAuthUser;
import com.docde.domain.auth.entity.AuthUser;
import com.docde.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "userService")
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("[DELETE] /users/{userId}")
    @WithMockAuthUser()
    void test1() throws Exception {
        Long userId = 1L;
        String password = "password";
        AuthUser authUser = AuthUser.builder().userRole(UserRole.ROLE_PATIENT).id(userId).patientId(1L).email("a@a.com").build();
        when(userService.hasPermissionToDeleteUser(userId, authUser)).thenReturn(true);
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(authUser);

        mockMvc.perform(delete("/users/{userId}", userId)
                        .header("X-User-Password", password)
                        .with(csrf())
                        .with(authentication(jwtAuthenticationToken)))
                .andExpect(status().isNoContent());
    }
}