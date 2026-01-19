package com.aimix_aimixapi.qna.dto.qna;

import lombok.Getter;
import lombok.Setter;

/**
 * QnA 질문 삭제 요청 DTO
 * 익명 게시글 삭제 시 비밀번호 필요
 */
@Getter
@Setter
public class QnaQuestionDeleteRequest {

    /**
     * 익명 게시글 비밀번호 (익명 게시글 삭제 시 필수)
     */
    private String anonymousPassword;
}

