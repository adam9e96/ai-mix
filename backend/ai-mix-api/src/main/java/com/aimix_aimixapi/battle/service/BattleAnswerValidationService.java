package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.entity.BattleQuestion;
import com.aimix_aimixapi.battle.entity.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 배틀 답변 검증 및 정규화 서비스
 * - 객관식 답변 형식 검증 및 정규화
 * - 주관식 답변 검증
 * - 문제 생성 시 객관식 정답 정규화
 * @since 2025-12-18
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleAnswerValidationService {

    private final BattleAnswerScoringService battleAnswerScoringService;

    /**
     * 답변 형식 검증 및 정규화 (사용자 답변 제출 시 사용)
     * <p>
     * 문제 타입에 따라 다른 검증 로직을 수행합니다:
     * - 객관식: 숫자 형식 검증 및 범위 검증 (1 ~ 선택지 개수)
     * - 주관식: 빈 값 검증만 수행
     *
     * @param question   배틀 문제
     * @param userAnswer 사용자 답변
     * @return 정규화된 답변
     * @throws IllegalArgumentException 답변 형식이 올바르지 않은 경우
     * @since 2025-12-18
     */
    public String validateAndNormalizeAnswer(BattleQuestion question, String userAnswer) {
        String trimmedAnswer = userAnswer.trim();

        return switch (question.getQuestionType()) {
            case OBJECTIVE -> validateAndNormalizeObjectiveAnswer(question, trimmedAnswer);
            case SUBJECTIVE -> validateSubjectiveAnswer(trimmedAnswer);
        };
    }

    /**
     * 객관식 답변 검증 및 정규화
     * <p>
     * 1. 선택지 개수 확인
     * 2. 숫자 형식 검증
     * 3. 범위 검증 (1 ~ 선택지 개수)
     *
     * @param question      배틀 문제
     * @param trimmedAnswer trim된 사용자 답변
     * @return 정규화된 답변 (문자열 형태의 숫자)
     * @throws IllegalArgumentException 답변 형식이 올바르지 않은 경우
     * @since 2025-12-18
     */
    private String validateAndNormalizeObjectiveAnswer(BattleQuestion question, String trimmedAnswer) {
        // 선택지 개수 확인
        int choiceCount = getChoiceCount(question);

        // 숫자 형식 검증 및 범위 검증
        try {
            int answerNum = Integer.parseInt(trimmedAnswer);

            if (answerNum < 1 || answerNum > choiceCount) {
                throw new IllegalArgumentException(
                        String.format("객관식 답변은 1부터 %d 사이의 숫자여야 합니다. 입력값: %s", choiceCount, trimmedAnswer));
            }

            return String.valueOf(answerNum);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("객관식 답변은 숫자여야 합니다. 입력값: %s", trimmedAnswer));
        }
    }

    /**
     * 주관식 답변 검증
     *
     * @param trimmedAnswer trim된 사용자 답변
     * @return 검증된 답변 (그대로 반환)
     * @throws IllegalArgumentException 답변이 비어있는 경우
     * @since 2025-12-18
     */
    private String validateSubjectiveAnswer(String trimmedAnswer) {
        if (trimmedAnswer.isEmpty()) {
            throw new IllegalArgumentException("주관식 답변은 비어있을 수 없습니다");
        }
        return trimmedAnswer;
    }

    /**
     * 문제의 선택지 개수 조회
     *
     * @param question 배틀 문제
     * @return 선택지 개수 (기본값: 4)
     * @since 2025-12-18
     */
    private int getChoiceCount(BattleQuestion question) {
        if (question.getChoices() == null) {
            return 4; // 기본값
        }

        List<String> choices = battleAnswerScoringService.parseChoicesFromJson(question.getChoices());
        if (choices != null && !choices.isEmpty()) {
            return choices.size();
        }

        return 4; // 기본값
    }

    /**
     * 객관식 정답 정규화 (문제 생성 시 사용)
     *
     * @param correctAnswer GPT가 생성한 정답
     * @param choiceCount   선택지 개수
     * @return 정규화된 정답 (1부터 choiceCount 사이의 숫자)
     * @throws IllegalArgumentException 정답 형식이 올바르지 않은 경우
     * @since 2025-12-18
     */
    public String normalizeObjectiveAnswer(String correctAnswer, int choiceCount) {
        String trimmed = correctAnswer.trim();

        // 숫자 형식 검증
        try {
            int answerNum = Integer.parseInt(trimmed);
            if (answerNum < 1 || answerNum > choiceCount) {
                throw new IllegalArgumentException(
                        String.format("객관식 정답은 1부터 %d 사이의 숫자여야 합니다. 입력값: %s",
                                choiceCount, trimmed));
            }
            return String.valueOf(answerNum);
        } catch (NumberFormatException e) {
            // 숫자가 아닌 경우 다른 형식 지원 시도
            return normalizeNonNumericAnswer(trimmed, choiceCount);
        }
    }

    /**
     * 숫자가 아닌 답변 형식을 숫자로 변환
     * <p>
     * 지원 형식:
     * - 영문: A, B, C, D (대소문자 무관) → 1, 2, 3, 4
     * - 한글: 가, 나, 다, 라 → 1, 2, 3, 4
     * - 특수: ①, ②, ③, ④ → 1, 2, 3, 4
     *
     * @param answer    답변 문자열
     * @param maxChoice 최대 선택지 개수
     * @return 정규화된 답변 (1부터 maxChoice 사이의 숫자)
     * @throws IllegalArgumentException 지원하지 않는 형식인 경우
     */
    public String normalizeNonNumericAnswer(String answer, int maxChoice) {
        String upper = answer.toUpperCase();

        // 영문 (A, B, C, D)
        if (upper.matches("^[A-Z]$")) {
            int num = upper.charAt(0) - 'A' + 1;
            if (num >= 1 && num <= maxChoice) {
                log.info("답변 정규화: {} → {}", answer, num);
                return String.valueOf(num);
            }
        }

        // 한글 (가, 나, 다, 라)
        if (answer.matches("^[가-라]$")) {
            int num = answer.charAt(0) - '가' + 1;
            if (num >= 1 && num <= maxChoice) {
                log.info("답변 정규화: {} → {}", answer, num);
                return String.valueOf(num);
            }
        }

        // 특수문자 (①, ②, ③, ④)
        if (answer.matches("^[①-④]$")) {
            int num = answer.charAt(0) - '①' + 1;
            if (num >= 1 && num <= maxChoice) {
                log.info("답변 정규화: {} → {}", answer, num);
                return String.valueOf(num);
            }
        }

        throw new IllegalArgumentException(
                String.format("객관식 답변은 1-%d 사이의 숫자 또는 A-%c 형식이어야 합니다. 입력값: %s",
                        maxChoice, (char) ('A' + maxChoice - 1), answer));
    }
}
