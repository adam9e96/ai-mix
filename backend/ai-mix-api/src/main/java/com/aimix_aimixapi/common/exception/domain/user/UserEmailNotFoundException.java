package com.aimix_aimixapi.common.exception.domain.user;

/**
 * 이메일로 사용자를 조회했으나 존재하지 않을 때 발생하는 예외
 */
public class UserEmailNotFoundException extends RuntimeException {
    public UserEmailNotFoundException(String message) {
        super(message);
    }
}
