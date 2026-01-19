package com.aimix_aimixapi.common.exception.domain.knowledge.card;

/**
 * 지식 카드 접근 권한 없음 예외
 * - 카드 수정 권한이 없을 때
 * - 카드 삭제 권한이 없을 때
 */
public class KnowledgeCardAccessDeniedException extends RuntimeException {
    public KnowledgeCardAccessDeniedException(String message) {
        super(message);
    }
}
