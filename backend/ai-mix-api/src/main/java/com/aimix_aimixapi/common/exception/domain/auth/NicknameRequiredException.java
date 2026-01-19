package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * 닉네임 필수 입력 예외
 */
public class NicknameRequiredException extends RuntimeException {
    public NicknameRequiredException(String message) {
        super(message);
    }
}
