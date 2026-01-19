package com.aimix_aimixapi.common.exception.domain.qna;

import java.util.UUID;

/**
 * GPT 답변 없음 예외
 * - 배틀 생성 시 GPT 답변이 없을 때 발생
 */
public class GptAnswerNotFoundException extends RuntimeException {
    public GptAnswerNotFoundException(String message) {
        super(message);
    }

    public GptAnswerNotFoundException(UUID questionId) {
        super("GPT 답변이 없어 배틀을 생성할 수 없습니다: " + questionId);
    }
}
