package com.aimix_aimixapi.knowledge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * QnA에서 개념 카드 생성 요청 DTO
 */
@Getter
@Setter
public class KnowledgeCardFromQnaRequest {

    /**
     * QnA 질문 ID
     */
    @NotNull(message = "질문 ID는 필수입니다")
    private UUID questionId;
}