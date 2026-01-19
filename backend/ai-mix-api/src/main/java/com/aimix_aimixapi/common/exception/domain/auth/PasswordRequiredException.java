package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 비밀번호 필수 입력 예외
 */
public class PasswordRequiredException extends RuntimeException {
    public PasswordRequiredException(String message) {
        super(message);
    }
}
