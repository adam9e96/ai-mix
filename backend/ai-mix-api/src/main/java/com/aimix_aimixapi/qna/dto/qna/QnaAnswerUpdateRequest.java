package com.aimix_aimixapi.qna.dto.qna;

import lombok.Getter;
import lombok.Setter;

/**
 * QnA 답변 수정 요청 DTO
 */
@Getter
@Setter
public class QnaAnswerUpdateRequest {

    /**
     * 답변 내용
     */
    private String body;
}

