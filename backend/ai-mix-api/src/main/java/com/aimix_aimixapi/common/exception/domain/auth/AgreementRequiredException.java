package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 개인정보 동의 필수 예외
 */
public class AgreementRequiredException extends RuntimeException {
    public AgreementRequiredException(String message) {
        super(message);
    }
}
