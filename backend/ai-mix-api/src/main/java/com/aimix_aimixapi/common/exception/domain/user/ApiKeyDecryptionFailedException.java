package com.aimix_aimixapi.common.exception.domain.user;

/**
 * API 키 복호화에 실패했을 때 발생하는 예외
 * - 암호화 키가 올바르지 않을 때
 * - 암호화된 데이터가 손상되었을 때
 */
public class ApiKeyDecryptionFailedException extends RuntimeException {
    public ApiKeyDecryptionFailedException(String message) {
        super(message);
    }

    public ApiKeyDecryptionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
