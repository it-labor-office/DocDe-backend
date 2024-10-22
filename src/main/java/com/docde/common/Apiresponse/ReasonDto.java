package com.docde.common.Apiresponse;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
public class ReasonDto {
    private HttpStatus httpStatus;
    private Integer statusCode;
    private String message;
}
