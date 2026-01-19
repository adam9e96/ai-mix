package com.aimix_aimixapi.battle.dto;

import com.aimix_aimixapi.battle.entity.QuestionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * 배틀 문제 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BattleQuestionDto {
    /**
     * 문제 ID
     */
    private UUID questionId;

    /**
     * 문제 내용
     */
    private String questionText;

    /**
     * 정답
     */
//    private String correctAnswer;

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
}

