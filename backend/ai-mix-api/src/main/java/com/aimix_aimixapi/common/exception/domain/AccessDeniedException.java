package com.aimix_aimixapi.common.exception.domain;

/**
 * 접근 권한이 없을 때 발생하는 예외
 * - 리소스는 존재하지만 해당 사용자에게 접근 권한이 없는 경우
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
