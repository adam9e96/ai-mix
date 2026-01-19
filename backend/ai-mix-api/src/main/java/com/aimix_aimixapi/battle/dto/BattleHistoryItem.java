package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 배틀 전적 항목 DTO
 * 전적 조회에서 각 배틀의 승패 정보를 나타냄
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleHistoryItem {
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
     * 전체 문제 수
     */
    private Integer totalQuestions;

    /**
     * 답변한 문제 수
     */
    private Integer answeredQuestions;

    /**
     * 정답 개수
     */
    private Integer correctCount;

    /**
     * 오답 개수
     */
    private Integer incorrectCount;

    /**
     * 평균 점수
     */
    private Double averageScore;

    /**
     * 정답률 (%)
     */
    private Double correctRate;

    /**
     * 승패 결과 (WIN, DRAW, LOSE)
     */
    private String result;

    /**
     * 완료 여부
     */
    private Boolean isCompleted;

    /**
     * 배틀 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 배틀 완료 시각 (마지막 답변 제출 시각)
     */
    private LocalDateTime completedAt;
}

