package com.docde.common.exceptions;

import com.docde.common.Apiresponse.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {
    private final BaseCode errorCode;

    public ApiException(BaseCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }
}