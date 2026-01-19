package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 배틀 간단 승패 결과 응답 DTO
 * Chat에서 배틀 완료 후 간단한 승패 결과를 보여주기 위한 경량 응답
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleSimpleResultResponse {

    /**
     * 배틀 ID
     */
    private UUID battleId;

    /**
     * 승패 결과 (WIN, DRAW, LOSE, IN_PROGRESS)
     */
    private String result;

    /**
     * 정답률 (%)
     */
    private Double correctRate;

    /**
     * 평균 점수
     */
    private Double averageScore;

    /**
     * 배틀 완료 시각 (마지막 답변 제출 시각)
     * 완료되지 않았으면 null
     */
    private LocalDateTime completedAt;
}
