package com.docde.config;

import com.docde.common.Apiresponse.ErrorStatus;
import com.docde.common.enums.TokenType;
import com.docde.common.exceptions.ApiException;
import com.docde.domain.auth.entity.UserDetailsImpl;
import com.docde.domain.auth.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtDecodeFilter extends OncePerRequestFilter {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            try {
                String authorization = request.getHeader("Authorization");

                if (authorization == null) throw new ApiException(ErrorStatus._NOT_FOUND_TOKEN);

                String token = jwtUtil.substringToken(authorization);
                TokenType tokenType = jwtUtil.getTokenType(token);
                if (!tokenType.equals(TokenType.ACCESS)) throw new ApiException(ErrorStatus._NOT_ACCESS_TOKEN);

                Claims claims = jwtUtil.extractClaims(token);

                if (claims == null) throw new ApiException(ErrorStatus._BAD_REQUEST_ILLEGAL_TOKEN);

                String email = claims.get("email", String.class);
                UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
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
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/auth", "/error"};
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
}
