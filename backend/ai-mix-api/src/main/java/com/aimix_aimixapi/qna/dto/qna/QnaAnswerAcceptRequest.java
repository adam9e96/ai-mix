package com.aimix_aimixapi.qna.dto.qna;

import lombok.Getter;
import lombok.Setter;

/**
 * QnA 답변 채택/해제 요청 DTO
 * 익명 질문의 경우 비밀번호를 함께 전달하기 위해 사용합니다.
 */
@Getter
@Setter
public class QnaAnswerAcceptRequest {

    /**
     * 익명 질문 비밀번호 (선택)
     * - 질문이 익명인 경우: 필수
     * - 질문이 일반 게시글인 경우: 사용되지 않음
     */
    private String anonymousPassword;
}