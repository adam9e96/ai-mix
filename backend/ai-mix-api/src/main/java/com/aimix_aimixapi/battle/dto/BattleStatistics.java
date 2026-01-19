package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배틀 통계 DTO
 * <p>
 * 배틀의 성적 및 진행 상황을 나타내는 통계 정보를 담는 클래스
 * <p>
 * 주요 용도:
 * - 배틀 결과 조회 시 성적 정보 제공
 * - 승패 판정을 위한 기준 데이터 제공
 * - 배틀 평가 및 피드백 생성에 활용
 * @since 2025-12-18
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleStatistics {

    /**
     * 총 문제 수
     * <p>
     * 배틀에 포함된 전체 문제의 개수입니다.
     * 배틀 생성 시 설정된 문제 개수와 동일합니다.
     */
    private Integer totalQuestions;

    /**
     * 답변한 문제 수
     * <p>
     * 사용자가 실제로 답변을 제출한 문제의 개수입니다.
     * 배틀 진행 중에는 totalQuestions보다 작을 수 있습니다.
     */
    private Integer answeredQuestions;

    /**
     * 정답 개수 (80점 이상)
     * <p>
     * 점수가 80점 이상인 답변의 개수입니다.
     * 80점 미만은 오답으로 간주됩니다.
     * <p>
     * 계산 방식: answerMap에서 score >= 80인 답변의 개수
     */
    private Integer correctCount;

    /**
     * 오답 개수 (80점 미만)
     * <p>
     * 점수가 80점 미만인 답변의 개수입니다.
     * <p>
     * 계산 방식: answeredQuestions - correctCount
     */
    private Integer incorrectCount;

    /**
     * 총점 (모든 문제 점수의 합)
     * <p>
     * 답변한 모든 문제의 점수를 합산한 값입니다.
     * 답변이 없는 문제는 0점으로 계산되지 않습니다.
     * <p>
     * 계산 방식: answerMap의 모든 answer.getScore()의 합
     */
    private Integer totalScore;

    /**
     * 평균 점수
     * <p>
     * 답변한 문제들의 평균 점수입니다.
     * 소수점 첫째 자리까지 반올림하여 표시됩니다.
     * <p>
     * 계산 방식: totalScore / answeredQuestions (answeredQuestions > 0인 경우)
     * 답변이 없는 경우 0.0을 반환합니다.
     */
    private Double averageScore;

    /**
     * 정답률 (%)
     * <p>
     * 답변한 문제 중 정답(80점 이상)의 비율을 퍼센트로 나타낸 값입니다.
     * 소수점 첫째 자리까지 반올림하여 표시됩니다.
     * <p>
     * 계산 방식: (correctCount / answeredQuestions) * 100 (answeredQuestions > 0인 경우)
     * 답변이 없는 경우 0.0을 반환합니다.
     * <p>
     * 승패 판정 기준:
     * - 80% 이상: WIN
     * - 50% 이상 80% 미만: DRAW
     * - 50% 미만: LOSE
     */
    private Double correctRate;

    /**
     * 완료 여부
     * <p>
     * 배틀의 모든 문제에 답변을 제출했는지 여부를 나타냅니다.
     * <p>
     * 계산 방식: answeredQuestions >= totalQuestions
     * <p>
     * true인 경우: 모든 문제에 답변 완료 (배틀 완료)
     * false인 경우: 아직 답변하지 않은 문제가 있음 (진행 중)
     */
    private Boolean isCompleted;
}