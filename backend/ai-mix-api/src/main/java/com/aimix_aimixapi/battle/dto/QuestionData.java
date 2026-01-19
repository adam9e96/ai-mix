package com.aimix_aimixapi.battle.dto;

import com.aimix_aimixapi.battle.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 문제 데이터 DTO
 * GPT로부터 생성된 문제 정보를 담는 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionData {
    private String questionText;
    private String correctAnswer;
    private String difficulty; // EASY, MEDIUM, HARD
    private QuestionType questionType; // SUBJECTIVE, OBJECTIVE
    private List<String> choices; // 객관식 선택지 (주관식인 경우 null)
}

