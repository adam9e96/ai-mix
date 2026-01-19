package com.aimix_aimixapi.common.exception.domain.user;

/**
 * 비밀번호 불일치 예외
 * - 비밀번호 검증 시 사용
 */
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) {
        super(message);
    }
}
