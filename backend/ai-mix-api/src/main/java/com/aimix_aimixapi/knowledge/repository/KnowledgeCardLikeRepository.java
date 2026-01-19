package com.aimix_aimixapi.knowledge.repository;

import com.aimix_aimixapi.knowledge.entity.KnowledgeCardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 개념 카드 좋아요 Repository
 * 개념 카드 좋아요 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface KnowledgeCardLikeRepository extends JpaRepository<KnowledgeCardLike, Long> {
    /**
     * 카드 ID와 사용자 ID로 좋아요 기록 조회
     * 
     * @param cardId 카드 ID
     * @param userId 사용자 ID
     * @return 좋아요 기록 (존재하는 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<KnowledgeCardLike> findByCardIdAndUserId(Long cardId, Long userId);

    /**
     * 카드 ID와 사용자 ID로 좋아요 기록 삭제
     * 
     * @param cardId 카드 ID
     * @param userId 사용자 ID
     * @apiNote 점검O
     * @since 2025-12-28
     */
    void deleteByCardIdAndUserId(Long cardId, Long userId);

    /**
     * 카드 ID로 모든 좋아요 기록 삭제 (카드 삭제 시 사용)
     * 
     * @param cardId 카드 ID
     * @apiNote 점검O
     * @since 2025-12-28
     */
    void deleteByCardId(Long cardId);

    /**
     * 카드 ID로 좋아요 수 집계
     * 
     * @param cardId 카드 ID
     * @return 해당 카드의 좋아요 수
     * @apiNote 점검O
     * @since 2025-12-28
     */
    long countByCardId(Long cardId);
}
