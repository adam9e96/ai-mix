package com.aimix_aimixapi.common.exception.domain.auth;

/**
 * User 엔티티 필수 예외
 * - UserDetailsImpl 생성 시 User 엔티티가 null인 경우 발생
 */
public class UserRequiredException extends RuntimeException {
    public UserRequiredException(String message) {
        super(message);
    }
}
