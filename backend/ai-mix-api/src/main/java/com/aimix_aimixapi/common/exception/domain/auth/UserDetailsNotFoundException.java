package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * UserDetails 조회 실패 예외
 * - UserDetailsService에서 사용자를 찾을 수 없을 때 발생
 */
public class UserDetailsNotFoundException extends RuntimeException {
    public UserDetailsNotFoundException(String message) {
        super(message);
    }
}
