package com.aimix_aimixapi.common.exception.domain.knowledge.card;

/**
 * 대화 내용이 없을 때 발생하는 예외
 * 카드 생성 시 대화 내용이 비어있을 때 발생
 */
public class EmptyConversationException extends RuntimeException {
    public EmptyConversationException(String message) {
        super(message);
    }
}
