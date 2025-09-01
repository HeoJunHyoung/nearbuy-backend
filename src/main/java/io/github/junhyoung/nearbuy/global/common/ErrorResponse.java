package io.github.junhyoung.nearbuy.global.common;

import io.github.junhyoung.nearbuy.global.exception.ErrorCode;
import lombok.Getter;


@Getter
public class ErrorResponse {
    private final int code;
    private final String message;

    ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
    }
}
