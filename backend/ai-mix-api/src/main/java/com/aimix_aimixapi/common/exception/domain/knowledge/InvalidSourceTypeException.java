package com.aimix_aimixapi.common.exception.domain.knowledge;

/**
 * 지원하지 않는 출처 타입 예외
 * SourceType이 지원되지 않는 값일 때 발생합니다.
 */
public class InvalidSourceTypeException extends RuntimeException {
    public InvalidSourceTypeException(String message) {
        super(message);
    }
}
