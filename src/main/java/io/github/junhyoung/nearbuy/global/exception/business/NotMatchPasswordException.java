package io.github.junhyoung.nearbuy.global.exception.business;

import io.github.junhyoung.nearbuy.global.exception.BusinessException;
import io.github.junhyoung.nearbuy.global.exception.ErrorCode;

public class NotMatchPasswordException extends BusinessException {
    public NotMatchPasswordException() {
        super(ErrorCode.NOT_MATCH_PASSWORD);
    }
}
