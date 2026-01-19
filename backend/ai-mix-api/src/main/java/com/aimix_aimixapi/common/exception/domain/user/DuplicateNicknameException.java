package com.aimix_aimixapi.common.exception.domain.user;

/**
 * 중복 닉네임 예외
 */
public class DuplicateNicknameException extends RuntimeException {
    public DuplicateNicknameException(String message) {
        super(message);
    }
}
