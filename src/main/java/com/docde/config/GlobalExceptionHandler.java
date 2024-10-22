package com.docde.config;

import com.docde.common.Apiresponse.ApiResponse;
import com.docde.common.Apiresponse.ReasonDto;
import com.docde.common.exceptions.ApiException;
import jakarta.validation.constraints.Null;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(InvalidRequestException.class)
//    public ResponseEntity<Map<String, Object>> invalidRequestExceptionException(InvalidRequestException ex) {
//        HttpStatus status = HttpStatus.BAD_REQUEST;
//        return getErrorResponse(status, ex.getMessage());
//    }
//
//    @ExceptionHandler(AuthException.class)
//    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
//        HttpStatus status = HttpStatus.UNAUTHORIZED;
//        return getErrorResponse(status, ex.getMessage());
//    }
//
//    @ExceptionHandler(ServerException.class)
//    public ResponseEntity<Map<String, Object>> handleServerException(ServerException ex) {
//        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
//        return getErrorResponse(status, ex.getMessage());
//    }
//
//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
//        HttpStatus status = HttpStatus.UNAUTHORIZED;
//        return getErrorResponse(status, ex.getMessage());
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
//        HttpStatus status = HttpStatus.UNAUTHORIZED;
//        return getErrorResponse(status, ex.getMessage());
//    }
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Null>> handleApiException(ApiException ex) {
        ReasonDto status = ex.getErrorCode().getReasonHttpStatus();
        return getErrorResponse(status.getHttpStatus(), status.getMessage());
    }
    public ResponseEntity<ApiResponse<Null>> getErrorResponse(HttpStatus status, String message) {

        return new ResponseEntity<>(ApiResponse.createError(message, status.value()), status);
    }
//    public ResponseEntity<Map<String, Object>> getErrorResponse(HttpStatus status, String message) {
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("status", status.name());
//        errorResponse.put("code", status.value());
//        errorResponse.put("message", message);
//
//        return new ResponseEntity<>(errorResponse, status);
//    }
}

