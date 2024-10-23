package com.docde.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum TokenType {
    ACCESS(4 * 60 * 60 * 1000), // 4hour
    REFRESH(24 * 60 * 60 * 1000); // 24hour

    private final long lifeTime;

    public static TokenType of(String role) {
        return Arrays.stream(TokenType.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 TokenType"));
    }
}
