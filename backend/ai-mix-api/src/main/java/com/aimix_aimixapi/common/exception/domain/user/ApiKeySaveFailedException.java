package com.aimix_aimixapi.common.exception.domain.user;

/**
 * API 키 저장에 실패했을 때 발생하는 예외
 * - 암호화 실패
 * - 데이터베이스 저장 실패
 */
public class ApiKeySaveFailedException extends RuntimeException {
    public ApiKeySaveFailedException(String message) {
        super(message);
    }

    public ApiKeySaveFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
