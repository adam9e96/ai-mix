package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 잘못된 요청 예외
 * - 요청 데이터가 null이거나 유효하지 않을 때 발생
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
