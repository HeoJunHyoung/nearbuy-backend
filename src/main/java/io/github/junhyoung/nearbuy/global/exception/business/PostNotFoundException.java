package io.github.junhyoung.nearbuy.global.exception.business;

import io.github.junhyoung.nearbuy.global.exception.BusinessException;
import io.github.junhyoung.nearbuy.global.exception.ErrorCode;
public class PostNotFoundException extends BusinessException {
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
