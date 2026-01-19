package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 배틀 목록 항목 DTO
 * 배틀 목록에서 각 배틀의 정보를 나타냄
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleListItem {
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
     * 완료 여부 (모든 문제에 답변했는지)
     */
    private Boolean isCompleted;

    /**
     * 평균 점수 (답변한 문제들의 평균)
     */
    private Double averageScore;

    /**
     * 생성 시각
     */
    private LocalDateTime createdAt;
}

