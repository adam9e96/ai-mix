package com.aimix_aimixapi.battle.dto;

import com.aimix_aimixapi.battle.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 문제별 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResult {

    /**
     * 문제 ID
     */
    private UUID questionId;

    /**
     * 문제 순서
     */
    private Integer orderNo;

    /**
     * 문제 내용
     */
    private String questionText;

    /**
     * 문제 유형 (SUBJECTIVE, OBJECTIVE)
     */
    private QuestionType questionType;

    /**
     * 문제 난이도 (EASY, MEDIUM, HARD)
     */
    private String difficulty;

    /**
     * 객관식 선택지 (주관식인 경우 null)
     */
    private List<String> choices;

    /**
     * 사용자 답변
     */
    private String userAnswer;

    /**
     * 정답
     */
    private String correctAnswer;

    /**
     * 획득 점수 (0-100)
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

    /**
     * 답변 제출 여부
     */
    private Boolean isAnswered;
}