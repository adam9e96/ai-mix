package com.aimix_aimixapi.knowledge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 개념 카드 응답 DTO
 */
@Getter
@Setter
@Builder
public class KnowledgeCardResponse {

    private Long id;
    private String title;
    private String slug;
    private String oneLineDefinition;
    private List<String> corePoints;
    private List<String> commonMistakes;
    private List<Long> relatedConcepts;
    private String sourceType;
    private UUID sourceId;
    private Long contributorId;
    private String contributorNickname;
    private Long viewCount;
    private Long upvoteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private Boolean isLiked; // 사용자가 좋아요했는지 여부 (자신이 만든 카드인 경우에만 반환)
}