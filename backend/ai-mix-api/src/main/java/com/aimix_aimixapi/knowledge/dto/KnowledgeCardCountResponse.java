package com.aimix_aimixapi.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 개수 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCardCountResponse {

    /**
     * 전체 카드 개수
     */
    private Long totalCount;

    /**
     * 공개된 카드 개수
     */
    private Long publishedCount;

    /**
     * 비공개 카드 개수
     */
    private Long unpublishedCount;
}
