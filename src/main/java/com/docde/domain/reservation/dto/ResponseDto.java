package com.docde.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResponseDto<T> {
    public String message;
    public int statusCode;
    public T data;

    public static <T> ResponseDto<T> of(int statusCode, String message, T data) {
        return new ResponseDto<T>(message, statusCode, data);
    }
}

