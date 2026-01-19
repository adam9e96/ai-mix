package com.aimix_aimixapi.battle.repository;

import com.aimix_aimixapi.battle.entity.BattleAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 배틀 답변 Repository
 * 배틀 답변 엔티티에 대한 데이터베이스 접근을 담당
 */
public interface BattleAnswerRepository extends JpaRepository<BattleAnswer, UUID> {
    /**
     * 배틀 ID로 모든 답변 조회
     * 
     * @param battleId 배틀 ID
     * @return 해당 배틀의 모든 답변 목록
     * @apiNote 점검O
     * @since 2025-12-28
     */
    List<BattleAnswer> findByBattleId(UUID battleId);

    /**
     * 배틀 ID와 문제 ID로 답변 조회 (중복 체크용)
     * 
     * @param battleId 배틀 ID
     * @param questionId 문제 ID
     * @return 해당 배틀과 문제에 대한 답변 (존재하는 경우에만)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    Optional<BattleAnswer> findByBattleIdAndQuestionId(UUID battleId, UUID questionId);
}
