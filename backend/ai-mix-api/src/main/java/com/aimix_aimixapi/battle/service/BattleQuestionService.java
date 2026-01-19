package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.dto.QuestionData;
import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.battle.entity.BattleQuestion;
import com.aimix_aimixapi.battle.repository.BattleQuestionRepository;
import com.aimix_aimixapi.common.exception.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

/**
 * 배틀 문제 서비스
 * <p>
 * 배틀 문제 관련 비즈니스 로직을 처리하는 서비스입니다.
 * <p>
 * 주요 기능:
 * - 배틀 문제 조회 및 검증
 * - 배틀 레벨 결정 (문제 난이도 기반)
 * - 여러 서비스에서 공통으로 사용되는 문제 관련 로직 제공
 *
 * @since 2025-12-18
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleQuestionService {

    private final BattleQuestionRepository battleQuestionRepository;

    /**
     * 문제 ID와 배틀으로 문제 조회 (검증 포함)
     * 문제가 해당 배틀에 속하는지 확인
     * 
     * @param questionId 문제 ID
     * @param battle 배틀 엔티티
     * @return 배틀 문제 엔티티
     * @throws ResourceNotFoundException 문제를 찾을 수 없거나 배틀에 속하지 않는 경우
     * @since 2025-12-10
     */
    @Transactional(readOnly = true)
    public BattleQuestion findByIdAndBattle(UUID questionId, Battle battle) {
        BattleQuestion question = battleQuestionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.warn("문제를 찾을 수 없습니다: questionId={}", questionId);
                    return new ResourceNotFoundException("문제를 찾을 수 없습니다: " + questionId);
                });

        // 문제가 해당 배틀에 속하는지 확인
        if (!question.getBattle().getId().equals(battle.getId())) {
            log.warn("문제가 배틀에 속하지 않습니다: questionId={}, battleId={}", 
                    questionId, battle.getId());
            throw new ResourceNotFoundException("문제가 배틀에 속하지 않습니다");
        }

        return question;
    }

    /**
     * 배틀 문제 저장
     *
     * @param question 배틀 문제 엔티티
     * @return 저장된 배틀 문제 엔티티
     * @since 2025-12-18
     */
    @Transactional
    public BattleQuestion save(BattleQuestion question) {
        log.debug("배틀 문제 저장: questionId={}, battleId={}", 
                question.getId(), question.getBattle().getId());
        return battleQuestionRepository.save(question);
    }

    /**
     * 문제들의 난이도에 따라 배틀 레벨 결정
     * <p>
     * 문제 목록의 난이도 분포를 분석하여 배틀의 전체 난이도 등급을 결정합니다.
     * <p>
     * 결정 기준:
     * - HARD 문제가 절반 이상: S 레벨
     * - MEDIUM이 많거나 HARD가 1개 이상: A 레벨
     * - EASY가 대부분: B 레벨
     * - 문제가 없는 경우: 기본값 B 레벨
     *
     * @param questions 문제 데이터 목록
     * @return 배틀 레벨 (S, A, B)
     * @since 2025-12-18
     */
    public String determineBattleLevel(List<QuestionData> questions) {
        // 기본값 B
        if (CollectionUtils.isEmpty(questions)) {
            return "B";
        }

        int hardCount = 0;
        int mediumCount = 0;
        int easyCount = 0;

        for (QuestionData question : questions) {
            String difficulty = question.getDifficulty() != null
                    ? question.getDifficulty().toUpperCase()
                    : "MEDIUM";

            switch (difficulty) {
                case "HARD" -> hardCount++;
                case "MEDIUM" -> mediumCount++;
                case "EASY" -> easyCount++;
                default -> mediumCount++;
            }
        }

        // HARD가 절반 이상이면 S
        if (hardCount >= questions.size() / 2.0) {
            return "S";
        }
        // MEDIUM이 많거나 HARD가 있으면 A
        else if (mediumCount > easyCount || hardCount > 0) {
            return "A";
        }
        // EASY가 대부분이면 B
        else {
            return "B";
        }
    }
}

