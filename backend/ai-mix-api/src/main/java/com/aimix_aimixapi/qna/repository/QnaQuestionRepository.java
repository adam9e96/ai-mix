package com.aimix_aimixapi.qna.repository;

import com.aimix_aimixapi.qna.entity.QnaQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * QnA 질문 Repository
 * QnA 질문 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface QnaQuestionRepository extends JpaRepository<QnaQuestion, UUID> {
    /**
     * 제목으로 질문 검색
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색된 질문 목록 (페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT q FROM QnaQuestion q WHERE " +
            "LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<QnaQuestion> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 작성자(닉네임)로 질문 검색
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색된 질문 목록 (페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT q FROM QnaQuestion q " +
            "LEFT JOIN q.user u " +
            "WHERE u IS NOT NULL AND LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<QnaQuestion> searchByAuthor(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 내용으로 질문 검색
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색된 질문 목록 (페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT q FROM QnaQuestion q WHERE " +
            "LOWER(q.body) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<QnaQuestion> searchByBody(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 태그로 질문 검색 (단일 태그)
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색된 질문 목록 (페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT DISTINCT q FROM QnaQuestion q " +
            "LEFT JOIN q.questionTags qt " +
            "LEFT JOIN qt.tag t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<QnaQuestion> searchByTag(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 다중 태그로 질문 검색 (OR 조건: 하나라도 일치하면)
     * 
     * @param tagNames 태그 이름 목록
     * @param pageable 페이지 정보
     * @return 검색된 질문 목록 (페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT DISTINCT q FROM QnaQuestion q " +
            "LEFT JOIN q.questionTags qt " +
            "LEFT JOIN qt.tag t " +
            "WHERE LOWER(t.name) IN :tagNames")
    Page<QnaQuestion> searchByTags(@Param("tagNames") List<String> tagNames, Pageable pageable);

    /**
     * 제목, 내용, 태그로 통합 검색
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색된 질문 목록 (페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT DISTINCT q FROM QnaQuestion q " +
            "LEFT JOIN q.questionTags qt " +
            "LEFT JOIN qt.tag t " +
            "WHERE (LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(q.body) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<QnaQuestion> searchByKeywordAndTags(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 최신순으로 질문 목록 조회
     * 
     * @param pageable 페이지 정보
     * @return 질문 목록 (생성일 내림차순 정렬, 페이지)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Page<QnaQuestion> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 모든 질문을 태그 정보와 함께 조회 (그래프 생성용)
     * Fetch Join을 사용하여 N+1 문제 방지
     * 
     * @return 태그 정보가 포함된 모든 질문 목록
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT DISTINCT q FROM QnaQuestion q " +
           "LEFT JOIN FETCH q.questionTags qt " +
           "LEFT JOIN FETCH qt.tag")
    List<QnaQuestion> findAllWithTags();

    /**
     * 사용자가 작성한 질문 수 조회 (익명 질문 제외)
     * 
     * @param user 사용자 엔티티
     * @return 작성한 질문 수
     * @apiNote 점검O
     * @since 2025-12-30
     */
    long countByUserAndIsAnonymousFalse(com.aimix_aimixapi.user.entity.User user);
}