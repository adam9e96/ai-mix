package com.aimix_aimixapi.battle.dto;

import com.aimix_aimixapi.battle.entity.QuestionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * GPT 문제 생성 응답 DTO
 * GPT API로부터 받은 문제 생성 응답을 파싱하기 위한 DTO
 * <p>JSON 형식 예시:
 * <pre>{@code
 * {
 *   "questions": [
 *     {
 *       "questionText": "문제 내용",
 *       "correctAnswer": "정답",
 *       "difficulty": "MEDIUM",
 *       "questionType": "OBJECTIVE",
 *       "choices": ["선택지1", "선택지2", "선택지3", "선택지4"]
 *     }
 *   ]
 * }
 * }</pre>
 * </p>
 *
 * @since 2025-12-18
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GptQuestionResponse {

    /**
     * 생성된 문제 목록
     * GPT가 생성한 문제들의 리스트입니다.
     * 일반적으로 3~5개의 문제가 포함됩니다.
     */
    @JsonProperty("questions")
    private List<QuestionItem> questions;

    /**
     * 개별 문제 항목 DTO
     *
     * <p>GPT가 생성한 개별 문제의 정보를 담는 내부 클래스입니다.
     * 각 문제는 제목, 정답, 난이도, 문제 유형, 선택지(객관식인 경우)를 포함합니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionItem {

        /**
         * 문제 내용
         * 사용자에게 제시될 문제의 본문입니다.
         * null이거나 빈 문자열일 수 없습니다.
         */
        @JsonProperty("questionText")
        private String questionText;

        /**
         * 정답
         *
         * <p>문제의 정답입니다. 문제 유형에 따라 형식이 다릅니다:</p>
         * <ul>
         *   <li>객관식(OBJECTIVE): "1", "2", "3", "4" 중 하나의 숫자 문자열</li>
         *   <li>주관식(SUBJECTIVE): 정답 텍스트</li>
         * </ul>
         * null이거나 빈 문자열일 수 없습니다.
         */
        @JsonProperty("correctAnswer")
        private String correctAnswer;

        /**
         * 문제 난이도
         *
         * <p>문제의 난이도를 나타냅니다. 가능한 값:</p>
         * <ul>
         *   <li>EASY: 기본 개념, 단순한 사실 확인</li>
         *   <li>MEDIUM: 개념 이해 및 적용 (기본값)</li>
         *   <li>HARD: 심화 개념, 종합적 사고 필요</li>
         * </ul>
         * null인 경우 기본값 "MEDIUM"이 사용됩니다.
         */
        @JsonProperty("difficulty")
        private String difficulty;

        /**
         * 문제 유형
         *
         * <p>문제의 유형을 나타냅니다. 가능한 값:</p>
         * <ul>
         *   <li>SUBJECTIVE: 주관식 문제 (기본값)</li>
         *   <li>OBJECTIVE: 객관식 문제</li>
         * </ul>
         * Jackson이 JSON 문자열을 자동으로 enum으로 변환합니다.
         * 알 수 없는 값이거나 null인 경우 {@link QuestionType#SUBJECTIVE}가 기본값으로 사용됩니다.
         */
        @JsonProperty("questionType")
        private QuestionType questionType;

        /**
         * 객관식 선택지 목록
         *
         * <p>객관식 문제인 경우 제공되는 선택지 목록입니다.</p>
         * <ul>
         *   <li>객관식(OBJECTIVE): 반드시 4개의 선택지를 포함해야 합니다.</li>
         *   <li>주관식(SUBJECTIVE): null이어야 합니다.</li>
         * </ul>
         *
         * <p>객관식의 경우 정답(correctAnswer)은 이 선택지 목록의 인덱스(1부터 시작)를
         * 나타내는 "1", "2", "3", "4" 중 하나의 문자열이어야 합니다.</p>
         */
        @JsonProperty("choices")
        private List<String> choices;
    }
}

