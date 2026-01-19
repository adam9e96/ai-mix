package com.aimix_aimixapi.auth.message;

import lombok.Getter;

/**
 * 인증/인가 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 */
@Getter
public enum AuthMessage {
    // 토큰 관련
    REFRESH_TOKEN_REQUIRED("RefreshToken이 필요합니다"),
    INVALID_REFRESH_TOKEN("유효하지 않은 RefreshToken입니다"),
    
    // 인증 관련
    AUTHENTICATION_FAILED("이메일 또는 비밀번호가 올바르지 않습니다"),
    USER_DETAILS_NOT_FOUND("사용자 정보를 찾을 수 없습니다: %s"),
    
    // 필수 입력 관련
    EMAIL_REQUIRED("이메일은 필수입니다"),
    NICKNAME_REQUIRED("닉네임은 필수입니다"),
    AGREEMENT_REQUIRED("개인정보 동의는 필수입니다"),
    PASSWORD_REQUIRED("비밀번호를 입력해주세요"),
    REQUEST_REQUIRED("회원가입 요청 정보가 필요합니다"),
    LOGIN_REQUIRED("로그인 요청 정보가 필요합니다"),
    USER_REQUIRED("User 엔티티는 필수입니다");


    private final String message;
    
    AuthMessage(String message) {
        this.message = message;
    }

    /**
     * 포맷팅된 메시지 반환
     * 
     * @param args 포맷 인자
     * @return 포맷팅된 메시지
     */
    public String format(Object... args) {
        return String.format(message, args);
    }
}
