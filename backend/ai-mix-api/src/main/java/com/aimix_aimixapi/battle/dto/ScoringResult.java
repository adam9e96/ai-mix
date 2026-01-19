package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채점 결과 DTO
 * - GPT 채점 결과 또는 기본 채점 로직의 결과를 담는 클래스
 * @since 2025-12-18
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringResult {
    /**
     * 채점 점수 (0-100)
     * - 0: 오답
     * - 1-79: 부분 점수
     * - 80-100: 정답으로 간주 (80점 이상)
     */
    private Integer score;

    /**
     * 채점 피드백
     * - GPT가 생성한 피드백 또는 기본 채점 로직의 피드백
     * - 한국어로 작성됨
     * - 사용자에게 학습에 도움이 되는 구체적인 피드백 제공
     */
    private String feedback;

    /**
     * 정답 여부
     * - true: 정답 (일반적으로 80점 이상)
     * - false: 오답 또는 부분 정답
     * - GPT 응답에서 제공되거나, 점수 기반으로 계산됨
     */
    private Boolean isCorrect;
}

