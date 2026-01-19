package com.aimix_aimixapi.chat.message;

import lombok.Getter;

/**
 * 채팅 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 */
@Getter
public enum ChatMessage {
    // 세션 조회 관련
    SESSION_NOT_FOUND("세션을 찾을 수 없습니다: %s"),
    SESSION_ACCESS_DENIED("이 세션에 접근할 권한이 없습니다"),
    
    // 메시지 조회 관련
    MESSAGE_NOT_FOUND("메시지를 찾을 수 없습니다: %s"),
    MESSAGE_UPDATE_ACCESS_DENIED("이 메시지를 수정할 권한이 없습니다"),
    MESSAGE_DELETE_ACCESS_DENIED("이 메시지를 삭제할 권한이 없습니다"),
    
    // 메시지 수정/삭제 관련
    AI_MESSAGE_CANNOT_BE_MODIFIED("AI 메시지는 수정할 수 없습니다"),
    AI_MESSAGE_CANNOT_BE_DELETED("AI 메시지는 삭제할 수 없습니다");

    private final String message;
    
    ChatMessage(String message) {
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
