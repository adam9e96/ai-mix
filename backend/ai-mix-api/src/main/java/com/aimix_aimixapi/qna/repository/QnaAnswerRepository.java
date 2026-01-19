package com.aimix_aimixapi.qna.repository;

import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * QnA 답변 Repository
 * QnA 답변 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, UUID> {
    /**
     * 질문으로 모든 답변 조회 (생성일 오름차순)
     * 
     * @param question 질문 엔티티
     * @return 해당 질문의 모든 답변 목록 (생성일 오름차순 정렬)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<QnaAnswer> findByQuestionOrderByCreatedAtAsc(QnaQuestion question);

    /**
     * 답변 ID와 사용자로 답변 조회 (권한 확인용)
     * 
     * @param id 답변 ID
     * @param user 사용자 엔티티
     * @return 해당 답변 (사용자가 소유한 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<QnaAnswer> findByIdAndUser(UUID id, User user);

    /**
     * 사용자가 작성한 답변 수 조회 (AI 답변 제외)
     * 
     * @param user 사용자 엔티티
     * @return 작성한 답변 수
     * @apiNote 점검O
     * @since 2025-12-30
     */
    long countByUserAndAnswerType(User user, com.aimix_aimixapi.qna.entity.AnswerType answerType);

    /**
     * 사용자가 작성한 채택된 답변 수 조회
     * 
     * @param user 사용자 엔티티
     * @return 채택된 답변 수
     * @apiNote 점검O
     * @since 2025-12-30
     */
    long countByUserAndIsAcceptedTrue(User user);

    /**
     * 사용자가 작성한 답변들의 총 점수 합계
     * 
     * @param user 사용자 엔티티
     * @return 총 점수 (답변이 없으면 0)
     * @apiNote 점검O
     * @since 2025-12-30
     */
    @Query("SELECT COALESCE(SUM(a.score), 0) FROM QnaAnswer a WHERE a.user = :user AND a.answerType = 'USER'")
    long sumScoreByUser(@Param("user") User user);
}