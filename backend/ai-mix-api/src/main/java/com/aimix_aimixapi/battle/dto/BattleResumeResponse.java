package com.aimix_aimixapi.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 배틀 재개 응답 DTO
 * 배틀을 이어서 할 때 사용되며, 배틀 정보와 각 문제의 진행 상태를 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleResumeResponse {
    /**
     * 배틀 ID
     */
    private UUID battleId;

    /**
     * 배틀 출제 소스 타입
     */
    private String sourceType;

    /**
     * 배틀 출제 소스 ID (세션 ID)
     */
    private UUID sourceId;

    /**
     * 배틀 난이도 등급
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
     * 다음 풀어야 할 문제 순서 (1부터 시작, 모든 문제를 풀었으면 null)
     * 예: 2번까지 풀었다면 nextQuestionOrder는 3
     */
    private Integer nextQuestionOrder;

    /**
     * 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 문제 목록 (진행 상태 포함)
     */
    private List<BattleQuestionWithProgressDto> questions;
}
