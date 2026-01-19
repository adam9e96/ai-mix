package com.aimix_aimixapi.knowledge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 지식 카드 존재 여부 확인 응답 DTO
 */
@Getter
@Setter
@Builder
public class KnowledgeCardExistsResponse {

    /**
     * 지식 카드 존재 여부
     */
    private Boolean exists;

    /**
     * 지식 카드 ID (존재할 때만)
     */
    private Long cardId;

    /**
     * 지식 카드 slug (존재할 때만, 프론트에서 이동할 때 사용)
     */
    private String slug;
}
