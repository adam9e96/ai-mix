package com.aimix_aimixapi.knowledge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 개념 카드 상세 응답 DTO
 */
@Getter
@Setter
@Builder
public class KnowledgeCardDetailResponse {

    private KnowledgeCardResponse card;
    private List<KnowledgeCardListResponse> relatedCards;
}