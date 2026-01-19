package com.aimix_aimixapi.common.exception.message;

/**
 * 리소스 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 * 공통 예외 메시지이므로 common 패키지에 위치
 */
public enum ResourceMessage {
    // 리소스 조회 관련
    NOT_FOUND("리소스를 찾을 수 없습니다"),
    QUESTION_NOT_FOUND("질문을 찾을 수 없습니다"),
    ANSWER_NOT_FOUND("답변을 찾을 수 없습니다");
    
    private final String message;
    
    ResourceMessage(String message) {
        this.message = message;
    }
    
    /**
     * 메시지 반환
     */
    public String getMessage() {
        return message;
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
