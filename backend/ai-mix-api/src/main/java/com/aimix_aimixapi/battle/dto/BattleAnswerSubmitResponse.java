package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 배틀 답변 제출 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleAnswerSubmitResponse {

    /**
     * 답변 ID
     */
    private UUID answerId;

    /**
     * 배틀 ID
     */
    private UUID battleId;

    /**
     * 문제 ID
     */
    private UUID questionId;

    /**
     * 사용자가 제출한 답변
     */
    private String userAnswer;

    /**
     * 정답
     */
    private String correctAnswer;

    /**
     * 점수 (0-100)
     */
    private Integer score;

    /**
     * AI 피드백
     */
    private String feedback;

    /**
     * 정답 여부
     */
    private Boolean isCorrect;
}

