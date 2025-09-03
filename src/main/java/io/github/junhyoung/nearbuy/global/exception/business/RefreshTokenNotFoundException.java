package io.github.junhyoung.nearbuy.global.exception.business;

import io.github.junhyoung.nearbuy.global.exception.BusinessException;
import io.github.junhyoung.nearbuy.global.exception.ErrorCode;

public class RefreshTokenNotFoundException extends BusinessException {
    public RefreshTokenNotFoundException(ErrorCode errorCode) {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}
