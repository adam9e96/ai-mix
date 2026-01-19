package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.config.BattleProperties;
import com.aimix_aimixapi.battle.dto.*;
import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.battle.entity.BattleAnswer;
import com.aimix_aimixapi.battle.entity.BattleQuestion;
import com.aimix_aimixapi.battle.entity.BattleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 배틀 통계 계산 서비스
 * <p>
 * 배틀의 성적 통계, 승패 판정, 전적 통계를 계산하는 서비스
 * <p>
 * 주요 기능:
 * - 배틀 통계 계산 (정답률, 평균 점수, 완료 여부 등)
 * - 승패 판정 (WIN, DRAW, LOSE, IN_PROGRESS)
 * - 배틀 전적 아이템 계산
 * - 전체 전적 통계 계산 (승률, 연승/연패 등)
 *
 * @since 2025-12-18
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleStatisticsService {

    private final BattleAnswerService battleAnswerService;
    private final BattleProperties battleProperties;

    /**
     * 정답 판정 기준 점수 조회
     * 
     * @return 정답 판정 기준 점수
     */
    public int getCorrectScoreThreshold() {
        return battleProperties.getCorrectScoreThreshold();
    }

    /**
     * 배틀 통계 계산
     * <p>
     * 배틀의 문제 목록과 답변 맵을 기반으로 통계를 계산.
     * <p>
     * 계산 항목:
     * - 총 문제 수, 답변한 문제 수
     * - 정답/오답 개수 (80점 기준)
     * - 총점, 평균 점수 (소수점 첫째 자리 반올림)
     * - 정답률 (소수점 첫째 자리 반올림)
     * - 완료 여부
     *
     * @param battle 배틀 엔티티
     * @param questions 배틀 문제 목록
     * @param answerMap 답변 맵 (문제 ID -> 답변)
     * @return 배틀 통계 정보
     * @since 2025-12-18
     */
    public BattleStatistics calculateStatistics(
            Battle battle,
            List<BattleQuestion> questions,
            Map<UUID, BattleAnswer> answerMap) {

        int totalQuestions = questions.size();
        int answeredQuestions = answerMap.size();

        // 정답/오답 개수 (80점 이상을 정답으로 간주)
        int correctCount = 0;
        int totalScore = 0;

        int correctScoreThreshold = getCorrectScoreThreshold();
        for (BattleAnswer answer : answerMap.values()) {
            totalScore += answer.getScore();
            if (answer.getScore() >= correctScoreThreshold) {
                correctCount++;
            }
        }

        int incorrectCount = answeredQuestions - correctCount;

        // 평균 점수 (소수점 첫째 자리 반올림)
        double averageScore = answeredQuestions > 0
                ? Math.round((double) totalScore / answeredQuestions * 10.0) / 10.0
                : 0.0;

        // 정답률 (소수점 첫째 자리 반올림)
        double correctRate = answeredQuestions > 0
                ? Math.round((double) correctCount / answeredQuestions * 1000.0) / 10.0
                : 0.0;

        // 완료 여부
        boolean isCompleted = answeredQuestions >= totalQuestions;

        return BattleStatistics.builder()
                .totalQuestions(totalQuestions)
                .answeredQuestions(answeredQuestions)
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .totalScore(totalScore)
                .averageScore(averageScore)
                .correctRate(correctRate)
                .isCompleted(isCompleted)
                .build();
    }

    /**
     * 승패 결과 판정
     * <p>
     * 배틀의 완료 여부와 정답률을 기반으로 승패를 판정합니다.
     * <p>
     * 판정 기준:
     * - 미완료 또는 답변이 없는 경우: IN_PROGRESS
     * - 정답률 winRateThreshold 이상: WIN
     * - 정답률 drawRateThreshold 이상 winRateThreshold 미만: DRAW
     * - 정답률 drawRateThreshold 미만: LOSE
     *
     * @param isCompleted 완료 여부
     * @param answeredQuestions 답변한 문제 수
     * @param correctRate 정답률 (%)
     * @return 승패 결과 enum
     * @since 2025-12-18
     */
    public BattleResult determineBattleResult(boolean isCompleted, int answeredQuestions, double correctRate) {
        if (!isCompleted || answeredQuestions == 0) {
            return BattleResult.IN_PROGRESS;
        }
        
        double winRateThreshold = battleProperties.getWinRateThreshold();
        double drawRateThreshold = battleProperties.getDrawRateThreshold();
        
        if (correctRate >= winRateThreshold) {
            return BattleResult.WIN;
        } else if (correctRate >= drawRateThreshold) {
            return BattleResult.DRAW;
        } else {
            return BattleResult.LOSE;
        }
    }

    /**
     * 배틀 전적 아이템 계산
     * <p>
     * 배틀 엔티티를 기반으로 전적 조회에 필요한 통계 정보를 계산.
     * <p>
     * 계산 항목:
     * - 답변 개수, 정답/오답 개수 (80점 기준)
     * - 평균 점수, 정답률
     * - 완료 여부, 완료 시각
     * - 승패 결과
     * <p>
     * 성능 최적화: DB 조회를 1번만 수행하여 모든 통계를 계산합니다.
     *
     * @param battle 배틀 엔티티
     * @return 배틀 전적 아이템
     * @since 2025-12-18
     */
    public BattleHistoryItem calculateBattleHistoryItem(Battle battle) {
        // 답변 목록을 한 번만 조회 (DB 조회 1번으로 최적화)
        List<BattleAnswer> answers = battleAnswerService.findByBattleId(battle.getId());

        int answeredCount = answers.size();
        int correctCount = 0;
        int incorrectCount = 0;
        int totalScore = 0;
        LocalDateTime completedAt = null;

        // 한 번의 순회로 모든 통계 계산
        int correctScoreThreshold = getCorrectScoreThreshold();
        for (BattleAnswer answer : answers) {
            int score = answer.getScore();
            totalScore += score;

            // 정답/오답 개수 계산
            if (score >= correctScoreThreshold) {
                correctCount++;
            } else {
                incorrectCount++;
            }

            // 가장 최근 답변 시각을 완료 시각으로 설정
            if (completedAt == null || answer.getCreatedAt().isAfter(completedAt)) {
                completedAt = answer.getCreatedAt();
            }
        }

        // 평균 점수 계산 (소수점 첫째 자리 반올림)
        double averageScore = answeredCount > 0
                ? Math.round((double) totalScore / answeredCount * 10.0) / 10.0
                : 0.0;

        // 정답률 계산 (소수점 첫째 자리 반올림)
        double correctRate = answeredCount > 0
                ? Math.round((double) correctCount / answeredCount * 1000.0) / 10.0
                : 0.0;

        // 완료 여부
        boolean isCompleted = answeredCount >= battle.getTotalQuestions();

        // 승패 판정
        BattleResult battleResult = determineBattleResult(isCompleted, answeredCount, correctRate);

        return BattleHistoryItem.builder()
                .battleId(battle.getId())
                .sourceType(battle.getSourceType().name())
                .level(battle.getLevel())
                .totalQuestions(battle.getTotalQuestions())
                .answeredQuestions(answeredCount)
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .averageScore(averageScore)
                .correctRate(correctRate)
                .result(battleResult.name())
                .isCompleted(isCompleted)
                .createdAt(battle.getCreatedAt())
                .completedAt(completedAt)
                .build();
    }

    /**
     * 전체 전적 통계 계산
     * <p>
     * 여러 배틀의 전적 아이템 목록을 기반으로 전체 통계를 계산합니다.
     * <p>
     * 계산 항목:
     * - 총 배틀 수, 승/무/패/진행중 개수
     * - 승률 (완료된 배틀 기준, 소수점 첫째 자리 반올림)
     * - 현재 연승/연패, 최대 연승/연패
     * <p>
     * 연승/연패 계산 규칙:
     * - WIN: 연승 증가, 연패 초기화
     * - LOSE: 연패 증가, 연승 초기화
     * - DRAW: 연승/연패 모두 초기화
     * - IN_PROGRESS: 연승/연패에 영향 없음
     *
     * @param historyItems 배틀 전적 아이템 목록 (시간순 정렬 권장)
     * @return 전체 전적 통계
     * @since 2025-12-18
     */
    public BattleHistoryStatistics calculateHistoryStatistics(List<BattleHistoryItem> historyItems) {
        long totalBattles = historyItems.size();
        long winCount = 0;
        long drawCount = 0;
        long loseCount = 0;
        long inProgressCount = 0;
        int currentWinStreak = 0;
        int maxWinStreak = 0;
        int currentLoseStreak = 0;
        int maxLoseStreak = 0;

        for (BattleHistoryItem item : historyItems) {
            BattleResult battleResult = BattleResult.fromString(item.getResult());
            if (battleResult == null) {
                continue;
            }
            
            switch (battleResult) {
                case WIN -> {
                    winCount++;
                    currentWinStreak++;
                    currentLoseStreak = 0;
                    if (currentWinStreak > maxWinStreak) {
                        maxWinStreak = currentWinStreak;
                    }
                }
                case DRAW -> {
                    drawCount++;
                    currentWinStreak = 0;
                    currentLoseStreak = 0;
                }
                case LOSE -> {
                    loseCount++;
                    currentLoseStreak++;
                    currentWinStreak = 0;
                    if (currentLoseStreak > maxLoseStreak) {
                        maxLoseStreak = currentLoseStreak;
                    }
                }
                case IN_PROGRESS -> {
                    inProgressCount++;
                    // 진행 중인 배틀은 연승/연패에 영향을 주지 않음
                }
            }
        }

        // 승률 계산 (완료된 배틀 기준)
        long completedBattles = winCount + drawCount + loseCount;
        double winRate = completedBattles > 0
                ? Math.round((double) winCount / completedBattles * 1000.0) / 10.0
                : 0.0;

        return BattleHistoryStatistics.builder()
                .totalBattles(totalBattles)
                .winCount(winCount)
                .drawCount(drawCount)
                .loseCount(loseCount)
                .inProgressCount(inProgressCount)
                .winRate(winRate)
                .currentWinStreak(currentWinStreak)
                .maxWinStreak(maxWinStreak)
                .currentLoseStreak(currentLoseStreak)
                .maxLoseStreak(maxLoseStreak)
                .build();
    }
}

