package com.aimix_aimixapi.common.exception.domain.qna;

/**
 * AI 답변 수정 불가 예외
 * - AI 답변은 수정할 수 없을 때 발생
 */
public class AiAnswerCannotBeModifiedException extends RuntimeException {
    public AiAnswerCannotBeModifiedException(String message) {
        super(message);
    }
}
