package com.aimix_aimixapi.user.message;

import lombok.Getter;

/**
 * 사용자 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 */
@Getter
public enum UserMessage {
    // 사용자 조회 관련
    EMAIL_NOT_FOUND("사용자를 찾을 수 없습니다: %s"),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다"),
    
    // 중복 관련
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다: %s"),
    DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다: %s"),
    
    // 비밀번호 관련
    PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다"),
    PASSWORD_REQUIRED("비밀번호를 입력해주세요");

    private final String message;
    
    UserMessage(String message) {
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
