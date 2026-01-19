package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * RefreshToken 필수 예외
 * - 토큰 재발급 시 RefreshToken이 제공되지 않은 경우
 */
public class RefreshTokenRequiredException extends RuntimeException {
    public RefreshTokenRequiredException(String message) {
        super(message);
    }
}
