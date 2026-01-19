package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.battle.entity.BattleAnswer;
import com.aimix_aimixapi.battle.entity.BattleQuestion;
import com.aimix_aimixapi.battle.repository.BattleAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 배틀 답변 서비스
 * <p>
 * 배틀 답변 관련 비즈니스 로직을 처리하는 서비스입니다.
 * <p>
 * 주요 기능:
 * - 배틀 답변 조회 및 저장
 * - 배틀별 답변 목록 조회
 * - 중복 답변 체크
 *
 * @since 2025-12-18
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleAnswerService {

    private final BattleAnswerRepository battleAnswerRepository;

    /**
     * 배틀별 답변 목록 조회
     *
     * @param battleId 배틀 ID
     * @return 배틀 답변 목록
     * @since 2025-12-18
     */
    @Transactional(readOnly = true)
    public List<BattleAnswer> findByBattleId(UUID battleId) {
        log.debug("배틀 답변 목록 조회: battleId={}", battleId);
        return battleAnswerRepository.findByBattleId(battleId);
    }

    /**
     * 배틀과 문제로 답변 조회 (중복 체크용)
     *
     * @param battleId   배틀 ID
     * @param questionId 문제 ID
     * @return 배틀 답변 (존재하는 경우)
     * @since 2025-12-18
     */
    @Transactional(readOnly = true)
    public Optional<BattleAnswer> findByBattleIdAndQuestionId(UUID battleId, UUID questionId) {
        log.debug("배틀 답변 조회: battleId={}, questionId={}", battleId, questionId);
        return battleAnswerRepository.findByBattleIdAndQuestionId(battleId, questionId);
    }

    /**
     * 배틀 답변 저장
     *
     * @param answer 배틀 답변 엔티티
     * @return 저장된 배틀 답변 엔티티
     * @since 2025-12-18
     */
    @Transactional
    public BattleAnswer save(BattleAnswer answer) {
        log.debug("배틀 답변 저장: answerId={}, battleId={}, questionId={}",
                answer.getId(), answer.getBattle().getId(), answer.getQuestion().getId());
        return battleAnswerRepository.save(answer);
    }

    /**
     * 배틀별 모든 답변 삭제
     *
     * @param answers 삭제할 답변 목록
     * @since 2025-12-18
     */
    @Transactional
    public void deleteAll(List<BattleAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return;
        }
        log.debug("배틀 답변 일괄 삭제: count={}", answers.size());
        battleAnswerRepository.deleteAll(answers);
    }
}
