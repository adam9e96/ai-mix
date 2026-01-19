package com.aimix_aimixapi.qna.dto.qna;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * QnA 질문 생성 요청 DTO
 */
@Getter
@Setter
public class QnaQuestionCreateRequest {

    /**
     * 질문 제목
     */
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    private String title;

    /**
     * 질문 내용
     */
    @NotBlank(message = "내용은 필수입니다")
    private String body;

    /**
     * 익명 여부
     */
    private Boolean isAnonymous = false;

    /**
     * 익명 게시글 비밀번호
     * 익명일 경우 필수
     */
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하여야 합니다")
    private String anonymousPassword;
}

