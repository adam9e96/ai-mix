package com.aimix_aimixapi.gpt.token.entity;

/**
 * GPT 사용 유형
 * 어떤 기능에서 GPT API를 사용했는지 구분합니다.
 */
public enum GptUsageType {
    /**
     * 채팅 메시지 생성
     */
    CHAT,

    /**
     * QnA AI 답변 생성
     */
    QNA,

    /**
     * 배틀 문제 생성
     */
    BATTLE_QUESTION,

    /**
     * 배틀 답변 채점
     */
    BATTLE_SCORING,

    /**
     * 지식 카드 생성
     */
    KNOWLEDGE_CARD
}
