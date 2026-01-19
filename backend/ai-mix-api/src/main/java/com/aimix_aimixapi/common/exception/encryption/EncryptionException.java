package com.aimix_aimixapi.common.exception.encryption;

/**
 * 암호화 관련 예외
 */
public class EncryptionException extends RuntimeException {
    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
