package com.aimix_aimixapi.battle.dto;

import com.aimix_aimixapi.battle.entity.QuestionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 배틀 문제 진행 상태 포함 DTO
 * 배틀 재개 시 사용되며, 각 문제의 답변 여부와 진행 상태를 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BattleQuestionWithProgressDto {
    /**
     * 문제 ID
     */
    private UUID questionId;

    /**
     * 문제 내용
     */
    private String questionText;

    /**
     * 문제 순서
     */
    private Integer orderNo;

    /**
     * 문제 유형
     * SUBJECTIVE: 주관식, OBJECTIVE: 객관식
     */
    private QuestionType questionType;

    /**
     * 객관식 선택지 (주관식인 경우 null)
     */
    private List<String> choices;

    /**
     * 답변 여부
     */
    private Boolean isAnswered;

    /**
     * 사용자가 제출한 답변 (답변한 경우에만 포함)
     */
    private String userAnswer;

    /**
     * 점수 (답변한 경우에만 포함)
     */
    private Integer score;

    /**
     * 피드백 (답변한 경우에만 포함)
     */
    private String feedback;

    /**
     * 정답 여부 (답변한 경우에만 포함)
     */
    private Boolean isCorrect;
}
