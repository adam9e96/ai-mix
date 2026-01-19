package com.aimix_aimixapi.common.exception.domain;

import java.util.UUID;

/**
 * 질문을 찾을 수 없을 때 발생하는 예외
 */
public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String message) {
        super(message);
    }

    public QuestionNotFoundException(UUID questionId) {
        super("질문을 찾을 수 없습니다: " + questionId);
    }
}
