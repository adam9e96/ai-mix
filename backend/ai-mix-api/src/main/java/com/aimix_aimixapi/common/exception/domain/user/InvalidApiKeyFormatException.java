package com.aimix_aimixapi.common.exception.domain.user;

/**
 * API 키 형식이 올바르지 않을 때 발생하는 예외
 * - OpenAI API 키는 "sk-"로 시작해야 함
 * - API 키가 null이거나 비어있을 때
 */
public class InvalidApiKeyFormatException extends RuntimeException {
    public InvalidApiKeyFormatException(String message) {
        super(message);
    }
}
