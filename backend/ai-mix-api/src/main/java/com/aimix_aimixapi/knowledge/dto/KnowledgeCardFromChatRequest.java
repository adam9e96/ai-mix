package com.aimix_aimixapi.knowledge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 챗봇에서 개념 카드 생성 요청 DTO
 */
@Getter
@Setter
public class KnowledgeCardFromChatRequest {

    /**
     * 챗봇 세션 ID
     */
    @NotNull(message = "세션 ID는 필수입니다")
    private UUID sessionId;
}