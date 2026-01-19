package com.aimix_aimixapi.qna.dto.qna;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * QnA 질문 수정 요청 DTO
 */
@Getter
@Setter
public class QnaQuestionUpdateRequest {

    /**
     * 질문 제목
     */
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    private String title;

    /**
     * 질문 내용
     */
    private String body;

    /**
     * 익명 여부
     */
    private Boolean isAnonymous;

    /**
     * 익명 게시글 비밀번호 (수정 시 검증용)
     */
    private String anonymousPassword;
}

