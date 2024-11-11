package com.docde.config;

import com.docde.common.enums.TokenType;
import com.docde.common.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_USER_ROLE = "userRole";
    public static final String CLAIM_PATIENT_ID = "patientId";
    public static final String CLAIM_DOCTOR_ID = "doctorId";
    public static final String CLAIM_HOSPITAL_ID = "hospitalId";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    @Value("${JWT_SECRET_TOKEN}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    private void init() {
        // 키 설정
        key = getSecretKeyFromBase64(secretKey);
    }

    private SecretKey getSecretKeyFromBase64(String base64) {
        return Keys.hmacShaKeyFor(Base64Coder.decode(base64));
    }

    public String createAccessToken(Long userId, String email, UserRole userRole, Long patientId, Long doctorId, Long hospitalId) {
        Date now = new Date();

        return BEARER_PREFIX + Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_USER_ROLE, userRole)
                .claim(CLAIM_PATIENT_ID, patientId)
                .claim(CLAIM_DOCTOR_ID, doctorId)
                .claim(CLAIM_HOSPITAL_ID, hospitalId)
                .claim(CLAIM_TOKEN_TYPE, TokenType.ACCESS)
                .expiration(new Date(now.getTime() + TokenType.ACCESS.getLifeTime()))
                .issuedAt(now)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId, String email, UserRole userRole, Long patientId, Long doctorId, Long hospitalId) {
        Date now = new Date();

        return BEARER_PREFIX + Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_USER_ROLE, userRole)
                .claim(CLAIM_PATIENT_ID, patientId)
                .claim(CLAIM_DOCTOR_ID, doctorId)
                .claim(CLAIM_HOSPITAL_ID, hospitalId)
                .claim(CLAIM_TOKEN_TYPE, TokenType.REFRESH)
                .expiration(new Date(now.getTime() + TokenType.REFRESH.getLifeTime()))
                .issuedAt(now)
                .signWith(key)
                .compact();
    }

    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new IllegalArgumentException("Not Found Token");
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public TokenType getTokenType(String token) {
        return TokenType.of((Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("tokenType", String.class)));
    }

    public boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}
