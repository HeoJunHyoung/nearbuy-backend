package io.github.junhyoung.nearbuy.global.exception.business;

import io.github.junhyoung.nearbuy.global.exception.BusinessException;
import io.github.junhyoung.nearbuy.global.exception.ErrorCode;

public class InvalidPasswordException extends BusinessException {
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}