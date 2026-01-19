package com.aimix_aimixapi.qna.dto.qna;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * QnA 답변 생성 요청 DTO
 */
@Getter
@Setter
public class QnaAnswerCreateRequest {

    /**
     * 질문 ID
     */
    @NotNull(message = "질문 ID는 필수입니다")
    private UUID questionId;

    /**
     * 답변 내용
     */
    @NotBlank(message = "답변 내용은 필수입니다")
    private String body;

    /**
     * 답변 타입: 'USER' 또는 'AI'
     * 기본값: 'USER'
     */
    private String answerType = "USER";
}

