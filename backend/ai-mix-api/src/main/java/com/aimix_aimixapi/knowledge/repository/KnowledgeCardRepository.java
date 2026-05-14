package com.aimix_aimixapi.knowledge.repository;

import com.aimix_aimixapi.knowledge.entity.KnowledgeCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 개념 카드 Repository
 * 개념 카드 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface KnowledgeCardRepository extends JpaRepository<KnowledgeCard, Long> {
    /**
     * 슬러그로 카드 조회
     * 
     * @param slug 슬러그
     * @return 카드 엔티티 (존재하는 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<KnowledgeCard> findBySlug(String slug);

    /**
     * 제목으로 카드 조회
     * 
     * @param title 카드 제목
     * @return 카드 엔티티 (존재하는 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<KnowledgeCard> findByTitle(String title);

    /**
     * 공개된 카드 목록 조회 (수정일 내림차순)
     * 
     * @param pageable 페이지 정보
     * @return 공개된 카드 목록 (수정일 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<KnowledgeCard> findByIsPublishedTrueOrderByUpdatedAtDesc(Pageable pageable);

    /**
     * 모든 카드 목록 조회 (수정일 내림차순)
     * 공개 여부와 관계없이 모두 조회
     * 
     * @param pageable 페이지 정보
     * @return 모든 카드 목록 (수정일 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<KnowledgeCard> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    /**
     * 키워드로 카드 검색 (제목, 한 줄 정의에서 검색)
     * 공개된 카드만 검색
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색된 카드 목록 (수정일 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT DISTINCT k FROM KnowledgeCard k " +
            "WHERE k.isPublished = true " +
            "AND (LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(k.oneLineDefinition) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY k.updatedAt DESC")
    Page<KnowledgeCard> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 키워드로 모든 카드 검색 (공개 여부와 관계없이)
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색된 카드 목록 (수정일 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT DISTINCT k FROM KnowledgeCard k " +
            "WHERE (LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(k.oneLineDefinition) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY k.updatedAt DESC")
    Page<KnowledgeCard> searchByKeywordAll(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 출처 타입과 출처 ID로 카드 조회
     * 
     * @param sourceType 출처 타입
     * @param sourceId 출처 ID
     * @return 카드 엔티티 (존재하는 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<KnowledgeCard> findBySourceTypeAndSourceId(
            com.aimix_aimixapi.knowledge.entity.SourceType sourceType, UUID sourceId);

    /**
     * 추천 수가 많은 카드 목록 조회 (공개된 카드만)
     * 
     * @param pageable 페이지 정보
     * @return 공개된 카드 목록 (추천 수 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<KnowledgeCard> findByIsPublishedTrueOrderByUpvoteCountDesc(Pageable pageable);

    /**
     * 조회수가 많은 카드 목록 조회 (공개된 카드만)
     * 
     * @param pageable 페이지 정보
     * @return 공개된 카드 목록 (조회수 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<KnowledgeCard> findByIsPublishedTrueOrderByViewCountDesc(Pageable pageable);

    /**
     * 추천 수가 많은 카드 목록 조회 (모든 카드)
     * 
     * @param pageable 페이지 정보
     * @return 모든 카드 목록 (추천 수 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<KnowledgeCard> findAllByOrderByUpvoteCountDesc(Pageable pageable);

    /**
     * 조회수가 많은 카드 목록 조회 (모든 카드)
     * 
     * @param pageable 페이지 정보
     * @return 모든 카드 목록 (조회수 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<KnowledgeCard> findAllByOrderByViewCountDesc(Pageable pageable);

    /**
     * 사용자가 생성한 카드 목록 조회 (수정일 내림차순)
     * 
     * @param contributor 기여자 엔티티
     * @param pageable 페이지 정보
     * @return 해당 사용자가 생성한 카드 목록 (수정일 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<KnowledgeCard> findByContributorOrderByUpdatedAtDesc(
            com.aimix_aimixapi.user.entity.User contributor, Pageable pageable);

    /**
     * 사용자가 생성한 카드 수 조회
     * 
     * @param contributor 기여자 엔티티
     * @return 해당 사용자가 생성한 카드 수
     * @apiNote 점검O
     * @since 2025-12-28
     */
    long countByContributor(com.aimix_aimixapi.user.entity.User contributor);

    /**
     * 공개된 카드 개수 조회
     * 
     * @return 공개된 카드 개수
     * @apiNote 점검O
     * @since 2025-12-28
     */
    long countByIsPublishedTrue();

    /**
     * 조회수 TOP10 카드 조회 (공개된 카드만)
     * 
     * @return 조회수 상위 10개 카드 목록 (조회수 내림차순 정렬)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<KnowledgeCard> findTop10ByIsPublishedTrueOrderByViewCountDesc();

    /**
     * 좋아요 수 TOP10 카드 조회 (공개된 카드만)
     * 
     * @return 좋아요 수 상위 10개 카드 목록 (좋아요 수 내림차순 정렬)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<KnowledgeCard> findTop10ByIsPublishedTrueOrderByUpvoteCountDesc();

    /**
     * 조회수 원자적 증가 (동시성 안전, lost update 방지)
     * UPDATE 쿼리로 해당 row만 수정하여 전체 엔티티 UPDATE 방지
     *
     * @param cardId 카드 ID
     * @since 2026-04-06
     */
    @Modifying
    @Query("UPDATE KnowledgeCard k SET k.viewCount = k.viewCount + 1 WHERE k.id = :cardId")
    void incrementViewCount(@Param("cardId") Long cardId);

    /**
     * 여러 카드 ID로 한 번에 조회 (N+1 방지)
     * IN 절로 관련 카드를 단일 쿼리로 조회
     *
     * @param ids 카드 ID 목록
     * @return 카드 목록
     * @since 2026-04-06
     */
    List<KnowledgeCard> findAllByIdIn(List<Long> ids);
}