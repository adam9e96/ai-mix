package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배틀 평가 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleEvaluation {

    /**
     * 등급 (S, A, B, C, D, F)
     */
    private String grade;

    /**
     * 등급 설명
     */
    private String gradeDescription;

    /**
     * 종합 평가 코멘트
     */
    private String comment;

    /**
     * 강점 (잘한 점)
     */
    private String strengths;

    /**
     * 약점 (개선할 점)
     */
    private String weaknesses;

    /**
     * 다음 학습 추천
     */
    private String recommendation;
}