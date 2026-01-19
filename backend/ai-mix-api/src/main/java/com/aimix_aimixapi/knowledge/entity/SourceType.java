package com.aimix_aimixapi.knowledge.entity;

/**
 * 지식백과 카드 출처 타입
 */
public enum SourceType {
    /**
     * QnA에서 생성된 카드
     */
    QNA,

    /**
     * 챗봇 대화에서 생성된 카드
     */
    CHAT,

    /**
     * 배틀에서 생성된 카드
     */
    BATTLE
}