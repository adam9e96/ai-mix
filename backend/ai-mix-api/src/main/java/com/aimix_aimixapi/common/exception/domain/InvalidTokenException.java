package com.aimix_aimixapi.common.exception.domain;

/**
 * 유효하지 않은 토큰 예외
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
