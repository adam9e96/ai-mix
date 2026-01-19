package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 유효하지 않은 RefreshToken 예외
 * - RefreshToken이 유효하지 않거나 검증에 실패한 경우
 */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
