package com.aimix_aimixapi.common.exception.message;

import lombok.Getter;

/**
 * 서버 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 * 공통 예외 메시지이므로 common 패키지에 위치
 */
@Getter
public enum ServerMessage {
    // 서버 오류 관련
    INTERNAL_ERROR("서버 내부 오류가 발생했습니다");

    /**
     * -- GETTER --
     *  메시지 반환
     */
    private final String message;
    
    ServerMessage(String message) {
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
