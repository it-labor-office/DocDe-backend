package com.docde.config;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.dto.AuthRequest;
import com.docde.domain.auth.dto.AuthResponse;
import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public AuthenticationFilter(JwtUtil jwtUtil, AuthenticationManager authenticationManager, HandlerExceptionResolver handlerExceptionResolver) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.handlerExceptionResolver = handlerExceptionResolver;
        setFilterProcessesUrl("/auth/signin");
        setUsernameParameter("email");
        setPasswordParameter("password");
    }

    // 로그인 시도
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            AuthRequest.SignIn signinRequest = new ObjectMapper().readValue(request.getInputStream(), AuthRequest.SignIn.class);
            return getAuthenticationManager()
                    .authenticate(new UsernamePasswordAuthenticationToken(signinRequest.email(), signinRequest.password(), null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 로그인 성공
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
        User user = userDetails.getUser();
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getUserRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), user.getUserRole());

        if (refreshToken == null) {
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            throw new ApiException(ErrorStatus._ERROR_WHILE_CREATE_TOKEN);
        }

        AuthResponse.SignIn signinResponse = new AuthResponse.SignIn(accessToken, refreshToken);
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(new ObjectMapper().writeValueAsString(signinResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        handlerExceptionResolver.resolveException(request, response, null, new ApiException(ErrorStatus._EMAIL_OR_PASSWORD_NOT_MATCHES));
    }
}
