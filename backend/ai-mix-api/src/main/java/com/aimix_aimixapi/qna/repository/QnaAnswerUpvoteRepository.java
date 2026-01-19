package com.aimix_aimixapi.qna.repository;

import com.aimix_aimixapi.qna.entity.QnaAnswerUpvote;
import com.aimix_aimixapi.qna.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * QnA 답변 추천 Repository
 * QnA 답변 추천 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface QnaAnswerUpvoteRepository extends JpaRepository<QnaAnswerUpvote, Long> {
    /**
     * 답변 ID와 사용자 ID로 추천 기록 조회
     * 
     * @param answerId 답변 ID
     * @param userId 사용자 ID
     * @return 추천 기록 (존재하는 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<QnaAnswerUpvote> findByAnswerIdAndUserId(UUID answerId, Long userId);

    /**
     * 답변 ID와 사용자 ID로 추천 기록 삭제
     * 
     * @param answerId 답변 ID
     * @param userId 사용자 ID
     * @apiNote 점검O
     * @since 2025-12-28
     */
    void deleteByAnswerIdAndUserId(UUID answerId, Long userId);

    /**
     * 답변 ID로 모든 추천 기록 삭제 (답변 삭제 시 사용)
     * 
     * @param answerId 답변 ID
     * @apiNote 점검O
     * @since 2025-12-28
     */
    void deleteByAnswerId(UUID answerId);

    /**
     * 답변 ID와 투표 타입으로 추천 수 집계
     * 
     * @param answerId 답변 ID
     * @param voteType 투표 타입
     * @return 해당 조건에 맞는 추천 수
     * @apiNote 점검O
     * @since 2025-12-28
     */
    @Query("SELECT COUNT(v) FROM QnaAnswerUpvote v WHERE v.answerId = :answerId AND v.voteType = :voteType")
    long countByAnswerIdAndVoteType(@Param("answerId") UUID answerId, @Param("voteType") VoteType voteType);
}