package com.aimix_aimixapi.common.exception.message;

/**
 * 요청 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 * 공통 예외 메시지이므로 common 패키지에 위치
 */
public enum RequestMessage {
    // 요청 검증 관련
    VALIDATION_FAILED("입력값 검증에 실패했습니다"),
    INVALID_REQUEST("요청 정보를 입력해주세요"),
    CONSTRAINT_VIOLATION("제약 조건 위반이 발생했습니다"),
    
    // 미디어 타입 관련
    UNSUPPORTED_MEDIA_TYPE("Content-Type은 'application/json'이어야 합니다. 현재 Content-Type: %s"),
    MESSAGE_NOT_READABLE("요청 본문을 파싱할 수 없습니다. JSON 형식이 올바른지 확인해주세요"),
    MESSAGE_NOT_READABLE_CONTENT_TYPE("Content-Type은 'application/json'이어야 합니다");
    
    private final String message;
    
    RequestMessage(String message) {
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
