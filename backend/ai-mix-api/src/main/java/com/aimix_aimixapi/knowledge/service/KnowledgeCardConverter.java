package com.aimix_aimixapi.knowledge.service;

import com.aimix_aimixapi.knowledge.dto.*;
import com.aimix_aimixapi.knowledge.entity.KnowledgeCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지식 카드 DTO 변환 서비스
 * KnowledgeCard 엔티티를 다양한 응답 DTO로 변환합니다.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class KnowledgeCardConverter {

    private final KnowledgeCardSlugService slugService;

    /**
     * KnowledgeCard를 KnowledgeCardResponse로 변환
     *
     * @param card 카드 엔티티 (null 불가)
     * @return 카드 응답 DTO
     * @apiNote 점검O
     * @since 2025-12-29
     */
    public KnowledgeCardResponse convertToCardResponse(KnowledgeCard card) {
        return convertToCardResponse(card, null);
    }

    /**
     * KnowledgeCard를 KnowledgeCardResponse로 변환 (좋아요 상태 포함)
     *
     * @param card    카드 엔티티 (null 불가)
     * @param isLiked 좋아요 여부 (null 가능, null이면 설정하지 않음)
     * @return 카드 응답 DTO
     * @apiNote 점검O
     * @since 2025-12-29
     */
    public KnowledgeCardResponse convertToCardResponse(KnowledgeCard card, Boolean isLiked) {
        return KnowledgeCardResponse.builder()
                .id(card.getId())
                .title(card.getTitle())
                .slug(card.getSlug())
                .oneLineDefinition(card.getOneLineDefinition())
                .corePoints(card.getCorePoints())
                .commonMistakes(card.getCommonMistakes())
                .relatedConcepts(card.getRelatedConcepts())
                .sourceType(card.getSourceType() != null ? card.getSourceType().name() : null)
                .sourceId(card.getSourceId())
                .contributorId(card.getContributor() != null ? card.getContributor().getId() : null)
                .contributorNickname(card.getContributor() != null ? card.getContributor().getNickname() : null)
                .viewCount(card.getViewCount())
                .upvoteCount(card.getUpvoteCount())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .isPublished(card.getIsPublished())
                .isLiked(isLiked)
                .build();
    }

    /**
     * KnowledgeCard를 KnowledgeCardListResponse로 변환
     *
     * @param card 카드 엔티티 (null 불가)
     * @return 카드 목록 응답 DTO
     * @apiNote 점검O
     * @since 2025-12-29
     */
    public KnowledgeCardListResponse convertToCardListResponse(KnowledgeCard card) {
        return KnowledgeCardListResponse.builder()
                .id(card.getId())
                .title(card.getTitle())
                .slug(card.getSlug())
                .oneLineDefinition(card.getOneLineDefinition())
                .viewCount(card.getViewCount())
                .upvoteCount(card.getUpvoteCount())
                .updatedAt(card.getUpdatedAt())
                .contributorNickname(card.getContributor() != null ? card.getContributor().getNickname() : null)
                .build();
    }

    /**
     * KnowledgeCardCreateRequest를 미리보기용 KnowledgeCardResponse로 변환
     * 저장하지 않은 카드 데이터를 미리보기용 응답으로 변환합니다.
     *
     * @param request 카드 생성 요청 DTO (null 불가)
     * @param userId  사용자 ID (null 불가)
     * @param userNickname 사용자 닉네임 (null 불가)
     * @return 미리보기용 카드 응답 DTO
     * @apiNote 점검O
     * @since 2025-12-29
     */
    public KnowledgeCardResponse convertToPreviewResponse(
            KnowledgeCardCreateRequest request, Long userId, String userNickname) {
        // 슬러그 생성 (임시, DB 조회 없이 생성)
        String slug = slugService.generateSlugForPreview(request.getTitle());

        return KnowledgeCardResponse.builder()
                .id(null) // 미리보기이므로 ID 없음
                .title(request.getTitle())
                .slug(slug)
                .oneLineDefinition(request.getOneLineDefinition())
                .corePoints(request.getCorePoints() != null ? request.getCorePoints() : new ArrayList<>())
                .commonMistakes(request.getCommonMistakes() != null ? request.getCommonMistakes() : new ArrayList<>())
                .relatedConcepts(request.getRelatedConcepts() != null ? request.getRelatedConcepts() : new ArrayList<>())
                .sourceType(request.getSourceType() != null ? request.getSourceType().name() : null)
                .sourceId(request.getSourceId())
                .contributorId(userId)
                .contributorNickname(userNickname)
                .viewCount(0L)
                .upvoteCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .build();
    }

    /**
     * KnowledgeCard 리스트를 KnowledgeCardListResponse 리스트로 변환
     *
     * @param cards 카드 엔티티 리스트 (null 불가)
     * @return 카드 목록 응답 DTO 리스트
     * @apiNote 점검O
     * @since 2025-12-29
     */
    public List<KnowledgeCardListResponse> convertToCardListResponseList(List<KnowledgeCard> cards) {
        return cards.stream()
                .map(this::convertToCardListResponse)
                .collect(Collectors.toList());
    }
}
