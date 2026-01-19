package com.aimix_aimixapi.qna.dto.tag;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * QnA 태그 생성 요청 DTO
 * questionId를 기반으로 질문을 조회하여 태그를 생성하기 위한 요청
 */
@Getter
@Setter
public class QnaTagGenerateRequest {

    /**
     * 질문 ID
     */
    @NotNull(message = "질문 ID는 필수입니다")
    private UUID questionId;
}
