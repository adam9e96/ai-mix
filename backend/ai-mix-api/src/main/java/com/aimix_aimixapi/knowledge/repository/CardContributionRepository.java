package com.aimix_aimixapi.knowledge.repository;

import com.aimix_aimixapi.knowledge.entity.CardContribution;
import com.aimix_aimixapi.knowledge.entity.KnowledgeCard;
import com.aimix_aimixapi.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 카드 기여 이력 Repository
 * 카드 기여 이력 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface CardContributionRepository extends JpaRepository<CardContribution, Long> {
    /**
     * 특정 카드의 기여 이력 조회
     * 
     * @param card 카드 엔티티
     * @return 해당 카드의 모든 기여 이력 목록
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<CardContribution> findByCard(KnowledgeCard card);

    /**
     * 특정 카드의 기여 이력 조회 (생성일 내림차순, 페이징)
     * 
     * @param card 카드 엔티티
     * @param pageable 페이지 정보
     * @return 해당 카드의 기여 이력 목록 (생성일 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<CardContribution> findByCardOrderByCreatedAtDesc(KnowledgeCard card, Pageable pageable);

    /**
     * 특정 사용자의 기여 이력 조회
     * 
     * @param contributor 기여자 엔티티
     * @return 해당 사용자의 모든 기여 이력 목록
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<CardContribution> findByContributor(User contributor);

    /**
     * 특정 사용자의 기여 이력 조회 (생성일 내림차순, 페이징)
     * 
     * @param contributor 기여자 엔티티
     * @param pageable 페이지 정보
     * @return 해당 사용자의 기여 이력 목록 (생성일 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<CardContribution> findByContributorOrderByCreatedAtDesc(User contributor, Pageable pageable);

    /**
     * 특정 카드와 사용자의 기여 이력 조회
     * 
     * @param card 카드 엔티티
     * @param contributor 기여자 엔티티
     * @return 해당 카드와 사용자의 기여 이력 목록
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<CardContribution> findByCardAndContributor(KnowledgeCard card, User contributor);
}