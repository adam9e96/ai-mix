package com.aimix_aimixapi.qna.repository;

import com.aimix_aimixapi.qna.entity.QnaQuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * QnA 질문-태그 매핑 Repository
 * QnA 질문과 태그 간의 매핑 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface QnaQuestionTagRepository extends JpaRepository<QnaQuestionTag, QnaQuestionTag.PK> {
    /**
     * 질문 ID로 태그 매핑 조회
     * 
     * @param questionId 질문 ID
     * @return 해당 질문의 모든 태그 매핑 목록
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<QnaQuestionTag> findByQuestionId(UUID questionId);

    /**
     * 질문 ID로 태그 매핑 삭제
     * 
     * @param questionId 질문 ID
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Modifying
    @Query("DELETE FROM QnaQuestionTag qqt WHERE qqt.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") UUID questionId);
}