package com.aimix_aimixapi.common.exception.domain.knowledge.contribution;

/**
 * 지식 카드 필수 예외
 * 기여 이력 기록 시 카드가 null인 경우 발생
 */
public class KnowledgeCardRequiredException extends RuntimeException {
    public KnowledgeCardRequiredException(String message) {
        super(message);
    }
}
