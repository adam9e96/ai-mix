package com.aimix_aimixapi.common.exception.domain.knowledge.card;

/**
 * 중복 지식 카드 예외
 * 이미 생성된 지식 카드가 존재할 때 발생
 */
public class DuplicateKnowledgeCardException extends RuntimeException {
    public DuplicateKnowledgeCardException(String message) {
        super(message);
    }
}
