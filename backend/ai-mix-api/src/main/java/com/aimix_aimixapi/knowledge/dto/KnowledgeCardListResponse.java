package com.aimix_aimixapi.knowledge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 개념 카드 목록 응답 DTO
 */
@Getter
@Setter
@Builder
public class KnowledgeCardListResponse {

    private Long id;
    private String title;
    private String slug;
    private String oneLineDefinition;
    private Long viewCount;
    private Long upvoteCount;
    private LocalDateTime updatedAt;
    private String contributorNickname;
    
    // 게임 카드 스타일 필드
    private String cardImageUrl;
    private String cardBackgroundUrl;
    private String rarity; // COMMON, RARE, EPIC, LEGENDARY
    private String frameColor; // HEX 색상 코드
    private Integer difficultyLevel; // 1-5
}