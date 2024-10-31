package com.docde.config;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.enums.TokenType;
import com.docde.common.enums.UserRole;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            try {
                String token = jwtUtil.substringToken(authorization);
                TokenType tokenType = jwtUtil.getTokenType(token);
                if (!tokenType.equals(TokenType.ACCESS)) throw new ApiException(ErrorStatus._NOT_ACCESS_TOKEN);

                Claims claims = jwtUtil.extractClaims(token);

                if (claims == null) throw new ApiException(ErrorStatus._BAD_REQUEST_ILLEGAL_TOKEN);

                Long id = Long.parseLong(claims.getSubject());
                String email = claims.get(JwtUtil.CLAIM_EMAIL, String.class);
                Long patientId = claims.get(JwtUtil.CLAIM_PATIENT_ID, Long.class);
                Long doctorId = claims.get(JwtUtil.CLAIM_DOCTOR_ID, Long.class);
                Long hospitalId = claims.get(JwtUtil.CLAIM_HOSPITAL_ID, Long.class);
                UserRole userRole = UserRole.of(claims.get(JwtUtil.CLAIM_USER_ROLE, String.class));
                AuthUser authUser = AuthUser.builder().id(id).email(email).userRole(userRole).patientId(patientId).doctorId(doctorId).hospitalId(hospitalId).build();
                JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (SecurityException | MalformedJwtException e) {
                throw new ApiException(ErrorStatus._UNAUTHORIZED_INVALID_TOKEN, e);
            } catch (ExpiredJwtException e) {
                throw new ApiException(ErrorStatus._UNAUTHORIZED_EXPIRED_TOKEN, e);
            } catch (UnsupportedJwtException e) {
                throw new ApiException(ErrorStatus._BAD_REQUEST_UNSUPPORTED_TOKEN, e);
            } catch (Exception e) {
                throw new ApiException(ErrorStatus._ERROR_WHILE_CREATE_TOKEN, e);
            }
        } catch (Exception e) {
            System.out.println(e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/auth", "/error"};
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
}
