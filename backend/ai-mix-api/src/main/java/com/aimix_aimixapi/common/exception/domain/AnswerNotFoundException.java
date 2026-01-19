package com.aimix_aimixapi.common.exception.domain;

import java.util.UUID;

/**
 * 답변을 찾을 수 없을 때 발생하는 예외
 */
public class AnswerNotFoundException extends RuntimeException {
    public AnswerNotFoundException(String message) {
        super(message);
    }

    public AnswerNotFoundException(UUID answerId) {
        super("답변을 찾을 수 없습니다: " + answerId);
    }
}
