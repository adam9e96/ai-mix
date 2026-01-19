package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 인증 실패 예외
 * - 로그인 시 사용자를 찾을 수 없거나 인증에 실패한 경우
 */
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
