package com.aimix_aimixapi.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 좋아요 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCardLikeResponse {

    /**
     * 좋아요 여부 (true: 좋아요됨, false: 좋아요 취소됨)
     */
    private Boolean isLiked;

    /**
     * 현재 좋아요 수
     */
    private Long upvoteCount;
}
