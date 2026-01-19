package com.aimix_aimixapi.battle.message;

import lombok.Getter;

/**
 * 배틀 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 */
@Getter
public enum BattleMessage {
    // 배틀 생성 관련
    CHAT_ID_REQUIRED("CHAT sourceType인 경우 id는 필수입니다"),
    QNA_ID_REQUIRED("QNA sourceType인 경우 id는 필수입니다"),
    CHAT_MESSAGES_EMPTY("세션에 대화 내용이 없습니다: %s"),
    QUESTION_GENERATION_FAILED("문제 생성에 실패했습니다. 내용을 확인해주세요."),
    
    // 배틀 조회 관련
    BATTLE_NOT_FOUND_BY_ID("배틀을 찾을 수 없습니다: %s"),
    
    // 배틀 접근 권한 관련
    BATTLE_ACCESS_DENIED("배틀에 접근할 권한이 없습니다"),
    
    // 배틀 명령 관련
    UNSUPPORTED_SOURCE_TYPE("지원하지 않는 sourceType입니다: %s"),
    ANSWER_ALREADY_EXISTS("이미 답변한 문제입니다. 답변을 수정하려면 기존 답변을 삭제 후 다시 제출해주세요."),
    
    // 선택지 파싱 관련
    CHOICES_PARSING_FAILED("선택지 파싱 실패: %s");

    private final String message;
    
    BattleMessage(String message) {
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
