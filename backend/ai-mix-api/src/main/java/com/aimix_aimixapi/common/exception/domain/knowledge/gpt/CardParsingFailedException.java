package com.aimix_aimixapi.common.exception.domain.knowledge.gpt;

/**
 * 카드 데이터 파싱 실패 예외
 * GPT 응답을 파싱하는 과정에서 실패할 때 발생
 */
public class CardParsingFailedException extends RuntimeException {
    public CardParsingFailedException(String message) {
        super(message);
    }

    public CardParsingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
