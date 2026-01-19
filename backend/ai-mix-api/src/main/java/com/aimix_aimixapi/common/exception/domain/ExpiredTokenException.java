package com.aimix_aimixapi.common.exception.domain;

/**
 * 만료된 토큰 예외
 */
public class ExpiredTokenException extends RuntimeException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}
