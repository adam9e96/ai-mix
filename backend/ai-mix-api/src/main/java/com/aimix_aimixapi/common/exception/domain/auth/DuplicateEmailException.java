package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 중복 이메일 예외
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
