package io.github.junhyoung.nearbuy.global.exception.business;

import io.github.junhyoung.nearbuy.global.exception.BusinessException;
import io.github.junhyoung.nearbuy.global.exception.ErrorCode;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException(ErrorCode errorCode) {
        super(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
