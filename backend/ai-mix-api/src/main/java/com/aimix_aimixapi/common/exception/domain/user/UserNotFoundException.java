package com.aimix_aimixapi.common.exception.domain.user;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 * - 인증된 사용자 정보가 없을 때
 * - 사용자 정보 조회 실패 시
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
