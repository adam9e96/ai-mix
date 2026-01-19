package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 배틀 결과 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleResultResponse {

    // ===== 배틀 기본 정보 =====
    /**
     * 배틀 ID
     */
    private UUID battleId;

    /**
     * 배틀 출제 소스 타입
     */
    private String sourceType;

    /**
     * 배틀 난이도 등급 (S, A, B)
     */
    private String level;

    /**
     * 배틀 결과 (win, lose 등)
     */
    private String result;

    /**
     * 배틀 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 배틀 완료 시각 (마지막 답변 제출 시각)
     */
    private LocalDateTime completedAt;

    // ===== 전체 통계 =====
    /**
     * 전체 통계 정보
     */
    private BattleStatistics statistics;

    // ===== 문제별 결과 =====
    /**
     * 문제별 상세 결과 목록
     */
    private List<QuestionResult> questionResults;

    // ===== 평가 결과 =====
    /**
     * 배틀 평가 결과
     */
    private
    BattleEvaluation evaluation;
}