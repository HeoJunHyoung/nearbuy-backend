package io.github.junhyoung.nearbuy.global.exception.business;

import io.github.junhyoung.nearbuy.global.exception.BusinessException;
import io.github.junhyoung.nearbuy.global.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
