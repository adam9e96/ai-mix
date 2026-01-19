package com.aimix_aimixapi.knowledge.message;

import lombok.Getter;

/**
 * 지식 카드 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 */
@Getter
public enum KnowledgeMessage {
    // 지식 카드 조회 관련
    CARD_NOT_FOUND("개념 카드를 찾을 수 없습니다: %s"),
    DUPLICATE_CARD("이미 생성된 지식 카드가 존재합니다: %s"),
    CARD_ACCESS_DENIED("카드를 수정할 권한이 없습니다"),
    
    // 카드 생성 관련
    EMPTY_CONVERSATION("대화 내용이 없어 카드를 생성할 수 없습니다"),
    CARD_GENERATION_FAILED("카드 생성에 실패했습니다: %s"),
    CARD_PARSING_FAILED("카드 데이터 파싱에 실패했습니다: %s"),
    
    // 필수 입력 관련
    CARD_REQUIRED("카드는 필수입니다"),
    CONTRIBUTOR_REQUIRED("기여자는 필수입니다"),
    CONTRIBUTION_TYPE_REQUIRED("기여 타입은 필수입니다"),
    
    // 출처 타입 관련
    INVALID_SOURCE_TYPE("지원하지 않는 출처 타입입니다: %s");

    private final String message;
    
    KnowledgeMessage(String message) {
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
