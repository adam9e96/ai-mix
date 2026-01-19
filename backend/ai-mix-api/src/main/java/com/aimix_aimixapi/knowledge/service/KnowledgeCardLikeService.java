package com.aimix_aimixapi.knowledge.service;

import com.aimix_aimixapi.common.exception.domain.knowledge.card.KnowledgeCardNotFoundException;
import com.aimix_aimixapi.knowledge.message.KnowledgeMessage;
import com.aimix_aimixapi.knowledge.dto.KnowledgeCardResponse;
import com.aimix_aimixapi.knowledge.entity.KnowledgeCard;
import com.aimix_aimixapi.knowledge.entity.KnowledgeCardLike;
import com.aimix_aimixapi.knowledge.repository.KnowledgeCardLikeRepository;
import com.aimix_aimixapi.knowledge.repository.KnowledgeCardRepository;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개념 카드 좋아요 서비스
 * 개념 카드에 대한 사용자 좋아요 기능을 처리합니다.
 * - 좋아요 추가/취소 (토글 기능)
 * - 사용자별 좋아요 상태 확인
 * - 카드의 추천 수(upvoteCount) 자동 관리
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class KnowledgeCardLikeService {

    private final KnowledgeCardLikeRepository cardLikeRepository;
    private final KnowledgeCardRepository cardRepository;
    private final UserService userService;

    /**
     * 개념 카드 좋아요 토글
     * 로그인한 사용자가 카드에 좋아요를 추가하거나 취소합니다.
     * - 이미 좋아요한 경우: 좋아요 취소 (upvoteCount 감소)
     * - 좋아요하지 않은 경우: 좋아요 추가 (upvoteCount 증가)
     *
     * @param email  사용자 이메일 (인증된 사용자 식별용, null 불가)
     * @param cardId 카드 ID (null 불가)
     * @return 업데이트된 카드 정보 (좋아요 수 포함)
     * @throws KnowledgeCardNotFoundException 카드를 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public KnowledgeCardResponse toggleLikeCard(String email, Long cardId) {
        log.info("카드 좋아요 토글 요청: email={}, cardId={}", email, cardId);

        // 사용자 조회
        User user = userService.findUserByEmail(email);

        // 카드 조회
        KnowledgeCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("카드를 찾을 수 없습니다: cardId={}", cardId);
                    return new KnowledgeCardNotFoundException(KnowledgeMessage.CARD_NOT_FOUND.format(String.valueOf(cardId)));
                });

        // 기존 좋아요 기록 확인
        var existingLike = cardLikeRepository.findByCardIdAndUserId(cardId, user.getId());

        if (existingLike.isPresent()) {
            // 이미 좋아요한 경우: 좋아요 취소
            cardLikeRepository.deleteByCardIdAndUserId(cardId, user.getId());
            card.decrementUpvoteCount();
            cardRepository.save(card);
            log.info("카드 좋아요 취소 완료: cardId={}, userId={}, upvoteCount={}", 
                    cardId, user.getId(), card.getUpvoteCount());
        } else {
            // 좋아요하지 않은 경우: 좋아요 추가
            KnowledgeCardLike like = KnowledgeCardLike.builder()
                    .cardId(cardId)
                    .userId(user.getId())
                    .card(card)
                    .user(user)
                    .build();
            cardLikeRepository.save(like);
            card.incrementUpvoteCount();
            cardRepository.save(card);
            log.info("카드 좋아요 추가 완료: cardId={}, userId={}, upvoteCount={}", 
                    cardId, user.getId(), card.getUpvoteCount());
        }

        // 업데이트된 카드 정보 반환
        return convertToCardResponse(card);
    }

    /**
     * KnowledgeCard를 KnowledgeCardResponse로 변환
     * 카드 엔티티를 응답 DTO로 변환합니다.
     *
     * @param card 카드 엔티티 (null 불가)
     * @return 카드 응답 DTO
     * @apiNote 점검O
     * @since 2025-12-29
     */
    private KnowledgeCardResponse convertToCardResponse(KnowledgeCard card) {
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
                .build();
    }

    /**
     * 사용자가 카드에 좋아요했는지 확인
     * 특정 사용자가 특정 카드에 좋아요를 눌렀는지 확인합니다.
     * - email이 null인 경우: false 반환
     * - 사용자를 찾을 수 없는 경우: false 반환 (예외를 던지지 않음)
     * - 조회 중 오류 발생 시: false 반환 (예외를 던지지 않음)
     *
     * @param email  사용자 이메일 (null 가능, null이면 false 반환)
     * @param cardId 카드 ID (null 불가)
     * @return 좋아요 여부 (true: 좋아요함, false: 좋아요하지 않음 또는 확인 불가)
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional(readOnly = true)
    public boolean isLikedByUser(String email, Long cardId) {
        if (email == null) {
            log.debug("이메일이 null입니다 - 좋아요 상태 확인 불가: cardId={}", cardId);
            return false;
        }

        try {
            User user = userService.findUserByEmail(email);
            boolean isLiked = cardLikeRepository.findByCardIdAndUserId(cardId, user.getId()).isPresent();
            log.debug("좋아요 상태 확인: email={}, cardId={}, isLiked={}", email, cardId, isLiked);
            return isLiked;
        } catch (Exception e) {
            log.warn("사용자 좋아요 상태 확인 실패: email={}, cardId={}", email, cardId, e);
            return false;
        }
    }

    /**
     * 카드의 모든 좋아요 기록 삭제
     * 카드 삭제 시 사용됩니다.
     *
     * @param cardId 카드 ID (null 불가)
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public void deleteAllLikesByCardId(Long cardId) {
        log.info("카드의 모든 좋아요 기록 삭제: cardId={}", cardId);
        cardLikeRepository.deleteByCardId(cardId);
        log.debug("카드의 모든 좋아요 기록 삭제 완료: cardId={}", cardId);
    }
}
