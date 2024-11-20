package com.docde.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final String message;
    private final Integer statusCode;
    private final T data;

    public static <T> ApiResponse<T> createSuccess(String message, Integer statusCode, T data) {
        return new ApiResponse<>(message, statusCode, data);
    }

    public static <T> ApiResponse<T> createError(String message, Integer statusCode) {
        return new ApiResponse<>(message, statusCode, null);
    }

    public static <T> ApiResponse<T> onSuccess(T result) {
        return new ApiResponse<>("Ok", 200, result);
    }

    public static <T> ApiResponse<T> onCreated(T result) {
        return new ApiResponse<>("Created", 201, result);
    }

    public static ApiResponse<String> onFailure(ErrorStatus errorStatus) {
        return new ApiResponse<>(errorStatus.getMessage(), errorStatus.getStatusCode(), null);
    }

    public ResponseEntity<ApiResponse<T>> toEntity() {
        return ResponseEntity.status(statusCode).body(this);
    }
}
