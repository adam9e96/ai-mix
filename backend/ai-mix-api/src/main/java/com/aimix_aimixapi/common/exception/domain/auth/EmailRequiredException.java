package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 이메일 필수 입력 예외
 */
public class EmailRequiredException extends RuntimeException {
    public EmailRequiredException(String message) {
        super(message);
    }
}
