package com.aimix_aimixapi.common.exception.domain.knowledge.gpt;

/**
 * 카드 생성 실패 예외
 * GPT API 호출 실패 등으로 카드 생성에 실패할 때 발생
 */
public class CardGenerationFailedException extends RuntimeException {

    public CardGenerationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
