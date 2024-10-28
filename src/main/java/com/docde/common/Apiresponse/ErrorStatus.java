package com.docde.common.Apiresponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.protocol.HTTP;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {

    _BAD_REQUEST_UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, 400, "지원되지 않는 JWT 토큰입니다."),
    _BAD_REQUEST_ILLEGAL_TOKEN(HttpStatus.BAD_REQUEST, 400, "잘못된 JWT 토큰입니다."),
    _UNAUTHORIZED_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 401, "유효하지 않는 JWT 서명입니다."),
    _UNAUTHORIZED_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 401, "만료된 JWT 토큰입니다."),
    _UNAUTHORIZED_TOKEN(HttpStatus.UNAUTHORIZED, 401, "JWT 토큰 검증 중 오류가 발생했습니다."),
    _ERROR_WHILE_CREATE_TOKEN(HttpStatus.UNAUTHORIZED, 401, "JWT 토큰 검증 중 오류가 발생했습니다."),
    _FORBIDDEN_TOKEN(HttpStatus.FORBIDDEN, 403, "관리자 권한이 없습니다."),
    _NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, 404, "JWT 토큰이 필요합니다."),
    _NOT_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 400, "리프레쉬 토큰이 아닙니다."),
    _NOT_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, 400, "엑세스 토큰이 아닙니다."),
    _EMAIL_MUST_BE_AUTHENTICATED(HttpStatus.BAD_REQUEST, 400, "이메일 인증되어야합니다."),
    _NO_MORE_STORE(HttpStatus.BAD_REQUEST, 400, "최대 3개 운영가능"),
    _TEST_ERROR(HttpStatus.BAD_REQUEST, 400, "ApiException 예외 처리 테스트"),

    //Auth,USer관련 코드
    _USERNAME_IS_SAME(HttpStatus.BAD_REQUEST, 400, "변경하려는 이름이 전과 동일합니다"),
    _NOT_FOUND_EMAIL(HttpStatus.BAD_REQUEST, 400, "이메일을 찾을 수 없습니다."),
    _DELETED_USER(HttpStatus.BAD_REQUEST, 400, "탈퇴한 계정입니다."),
    _EMAIL_OR_PASSWORD_NOT_MATCHES(HttpStatus.BAD_REQUEST, 400, "이메일 또는 비밀번호가 틀렸습니다."),
    _DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, 400, "중복된 이메일입니다."),
    _INVALID_EMAIL_FORM(HttpStatus.BAD_REQUEST, 400, "이메일 형식이 올바르지 않습니다."),
    _INVALID_PASSWORD_FORM(HttpStatus.BAD_REQUEST, 400, "비밀번호는 최소 8자 이상이어야 하며, 대소문자 포함 영문, 숫자, 특수문자를 최소 1글자씩 포함해야 합니다."),
    _INVALID_PHONE_FORM(HttpStatus.BAD_REQUEST, 400, "전화번호는 11글자의 숫자로만 이루어진 문자열이여야 합니다."),
    _INVALID_USER_INFO(HttpStatus.BAD_REQUEST, 400, "변경하려는 정보가 잘못되었습니다."),
    _INVALID_BIRTHDAY(HttpStatus.BAD_REQUEST, 400, "잘못된 생일 값입니다"),
    _BAD_REQUEST_NOT_FOUND_USER(HttpStatus.BAD_REQUEST, 400, "해당 유저를 찾을 수 없습니다."),
    _PASSWORD_IS_DUPLICATED(HttpStatus.BAD_REQUEST, 400, "이미 사용중인 비밀번호로 변경할 수 없습니다."),
    _INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, 400, "잘못된 유저권한 입니다."),
    _USER_ROLE_IS_NULL(HttpStatus.BAD_REQUEST, 400, "유저 권한이 없습니다."),
    _INVALID_USER_NAME(HttpStatus.BAD_REQUEST, 400, "유저이름은 최소 3자 이상,20자 이하여야 하며, 대소문자 포함 영문,숫자만 사용가능합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, 403, "권한이 없습니다."),
    _ERROR_WHILE_SENDING_EMAIL(HttpStatus.FORBIDDEN, 500, "이메일을 보내는 중 오류가 발생하였습니다."),

    //병원 관련 코드
    _NOT_FOUND_HOSPITAL(HttpStatus.BAD_REQUEST, 400, "병원을 찾을 수 없습니다"),

    //예약 관련 코드
    _NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, 404, "예약을 찾을 수 없습니다."),
    _BAD_REQUEST_RESERVATION_REASON(HttpStatus.BAD_REQUEST, 400, "예약 사유가 없으면 안됩니다."),
    _BAD_REQUEST_RESERVATION_REJECT_REASON(HttpStatus.BAD_REQUEST, 400, "거부 사유가 없으면 안됩니다."),
    _ALREADY_CANCEL_RESERVATION(HttpStatus.BAD_REQUEST, 400, "이미 취소된 예약입니다."),
    _NOT_RESERVED_RESERVATION(HttpStatus.BAD_REQUEST, 400, "이미 승인된 예약이 아닙니다."),
    _ALREADY_DONE_RESERVATION(HttpStatus.BAD_REQUEST, 400, "이미 진료가 끝난 예약입니다."),
    _DENIED_RESERVATION(HttpStatus.BAD_REQUEST, 400, "진료가 거부된 예약입니다."),
    _ALREADY_RESERVED_RESERVATION(HttpStatus.BAD_REQUEST, 400, "이미 예약이 승인 되었습니다."),

    // 의사 관련 코드
    _NOT_FOUND_DOCTOR(HttpStatus.NOT_FOUND, 404, "의사를 찾을 수 없습니다."),
    _FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, 403, "해당 병원에 소속된 의사가 아닙니다."),

    // 환자 관련 코드
    _NOT_FOUND_PATIENT(HttpStatus.NOT_FOUND, 404, "환자를 찾을 수 없습니다."),

    // 진기록 관련 코드
    _NOT_FOUND_MEDICAL_RECORD(HttpStatus.NOT_FOUND, 404, "진료기록을 찾을 수 없습니다."),
    _UNAUTHORIZED_ACCESS_MEDICAL_RECORD(HttpStatus.UNAUTHORIZED,403,"진료기록에 대한 접근이 허용되지 않습니다."),

    // 리뷰 관련 코드
    _NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, 404,    "해당 리뷰를 찾을 수 없습니다."),
    _FORBIDDEN_REVIEW_ACCESS(HttpStatus.FORBIDDEN, 403, "리뷰를 작성한 사용자가 아닙니다."),

    //접수 관련 코드
    _BAD_REQUEST_ALREADY_CHECKED_IN(HttpStatus.BAD_REQUEST, 400, "이미 진행중인 접수가 있습니다."),
    _BAD_REQUEST_DOCTOR_NOT_BELONG_TO_HOSPITAL(HttpStatus.BAD_REQUEST, 400, "해당 병원에 소속된 의사가 아닙니다."),
    _NOT_FOUND_CHECK_IN(HttpStatus.BAD_REQUEST, 400, "진행 중인 진료 접수가 없습니다."),
    _FORBIDDEN_DOCTOR_NOT_BELONG_TO_HOSPITAL(HttpStatus.FORBIDDEN, 403, "해당 병원에 소속된 의사가 아닙니다.");

    private final HttpStatus httpStatus;
    private final Integer statusCode;
    private final String message;

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .statusCode(statusCode)
                .httpStatus(httpStatus)
                .message(message)
                .build();
    }
}
