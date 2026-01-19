package com.aimix_aimixapi.qna.message;

import lombok.Getter;

/**
 * QNA 예외 메시지
 * 안 바뀌는 예외 메시지를 enum으로 관리
 */
@Getter
public enum QnaMessage {
    // 질문 생성 관련
    ANONYMOUS_PASSWORD_REQUIRED("익명 게시글은 비밀번호가 필수입니다"),
    LOGIN_REQUIRED_FOR_NORMAL_POST("일반 게시글 작성을 위해서는 로그인이 필요합니다"),
    
    // 질문 수정 관련
    PASSWORD_REQUIRED_FOR_ANONYMOUS("익명 게시글로 변경하려면 비밀번호가 필요합니다"),
    LOGIN_REQUIRED_FOR_NORMAL_POST_CHANGE("일반 게시글로 변경하려면 로그인이 필요합니다"),
    
    // 답변 관련
    AI_ANSWER_CANNOT_BE_MODIFIED("AI 답변은 수정할 수 없습니다"),
    
    // 배틀 데이터 수집 관련
    GPT_ANSWER_NOT_FOUND("GPT 답변이 없어 배틀을 생성할 수 없습니다: %s"),
    
    // 권한 검증 관련
    ANSWER_NO_PERMISSION("답변에 대한 권한이 없습니다"),
    ANSWER_MODIFY_LOGIN_REQUIRED("답변 수정/삭제를 위해서는 로그인이 필요합니다"),
    PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다"),
    ANONYMOUS_PASSWORD_REQUIRED_FOR_ACTION("익명 게시글 %s을(를) 위해서는 비밀번호가 필요합니다"),
    LOGIN_REQUIRED_FOR_ACTION("%s을(를) 위해서는 로그인이 필요합니다"),
    ONLY_QUESTION_AUTHOR_CAN_ACCEPT("해당 질문의 작성자만 답변을 채택/해제할 수 있습니다"),
    NO_PERMISSION_FOR_ACTION("%s에 대한 권한이 없습니다");

    private final String message;
    
    QnaMessage(String message) {
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
