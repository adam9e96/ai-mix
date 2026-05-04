package com.aimix_aimixapi.battle.repository;

import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 배틀 Repository
 * 배틀 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface BattleRepository extends JpaRepository<Battle, UUID> {
    /**
     * 사용자로 모든 배틀 조회 (생성일 내림차순)
     *
     * @param user 사용자 엔티티
     * @return 해당 사용자의 모든 배틀 목록 (생성일 내림차순 정렬)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<Battle> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 사용자의 모든 배틀을 질문/답변과 함께 조회 (N+1 방지)
     * Fetch Join으로 배틀 + 질문 + 답변을 한 번의 쿼리로 로딩
     * DISTINCT로 카테시안 곱 중복 제거
     *
     * @param user 사용자 엔티티
     * @return 해당 사용자의 모든 배틀 목록 (질문/답변 포함, 생성일 내림차순)
     * @since 2026-04-06
     */
    @Query("SELECT DISTINCT b FROM Battle b " +
            "LEFT JOIN FETCH b.questions " +
            "LEFT JOIN FETCH b.answers " +
            "WHERE b.user = :user " +
            "ORDER BY b.createdAt DESC")
    List<Battle> findByUserWithQuestionsAndAnswers(@Param("user") User user);

    /**
     * 배틀 ID와 사용자로 배틀을 질문/답변과 함께 조회 (N+1 방지)
     *
     * @param battleId 배틀 ID
     * @param user 사용자 엔티티
     * @return 해당 배틀 (질문/답변 포함, 사용자가 소유한 경우에만)
     * @since 2026-04-06
     */
    @Query("SELECT b FROM Battle b " +
            "LEFT JOIN FETCH b.questions " +
            "LEFT JOIN FETCH b.answers " +
            "WHERE b.id = :battleId AND b.user = :user")
    Optional<Battle> findByIdAndUserWithQuestionsAndAnswers(
            @Param("battleId") UUID battleId, @Param("user") User user);

    /**
     * 배틀 ID와 사용자로 배틀 조회 (권한 확인용)
     *
     * @param battleId 배틀 ID
     * @param user 사용자 엔티티
     * @return 해당 배틀 (사용자가 소유한 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<Battle> findByIdAndUser(UUID battleId, User user);

    /**
     * 사용자별 배틀 개수 조회 (통계용)
     * - 마이페이지 통계 정보 조회에 사용
     * - COUNT 쿼리로 최적화되어 전체 데이터를 로드하지 않음
     *
     * @param user 사용자 엔티티
     * @return 해당 사용자의 배틀 참여 횟수
     * @apiNote 점검O
     * @since 2025-12-28
     */
    long countByUser(User user);
}
