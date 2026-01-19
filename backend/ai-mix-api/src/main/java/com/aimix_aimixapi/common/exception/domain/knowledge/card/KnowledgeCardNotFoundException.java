package com.aimix_aimixapi.common.exception.domain.knowledge.card;

/**
 * 지식 카드를 찾을 수 없을 때 발생하는 예외
 * - 카드 ID로 조회 실패 시
 * - 카드 slug로 조회 실패 시
 */
public class KnowledgeCardNotFoundException extends RuntimeException {
    public KnowledgeCardNotFoundException(String message) {
        super(message);
    }
}
