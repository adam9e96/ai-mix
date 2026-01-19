package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 요청 정보 필수 예외
 */
public class RequestRequiredException extends RuntimeException {
    public RequestRequiredException(String message) {
        super(message);
    }
}
