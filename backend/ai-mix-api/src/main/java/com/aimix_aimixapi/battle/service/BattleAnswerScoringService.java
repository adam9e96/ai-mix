package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.dto.GptScoringResponse;
import com.aimix_aimixapi.battle.dto.ScoringResult;
import com.aimix_aimixapi.battle.entity.BattleQuestion;
import com.aimix_aimixapi.gpt.service.GptService;
import com.aimix_aimixapi.common.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 배틀 채점 서비스
 * - GPT를 이용한 답변 채점 및 피드백 생성
 * - 객관식 답변 직접 채점
 * - 기본 채점 로직 (GPT 실패 시)
 * 
 * @since 2025-12-18
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleAnswerScoringService {

    private final GptService gptService;
    private final JsonUtils jsonUtils;
    private final com.aimix_aimixapi.battle.config.BattleProperties battleProperties;

    /**
     * GPT를 이용한 답변 채점 및 피드백 생성
     *
     * @param question   배틀 문제
     * @param userAnswer 사용자 답변
     * @return 채점 결과
     * @since 2025-12-18
     */
    public ScoringResult scoreAnswerWithGpt(BattleQuestion question, String userAnswer,
            com.aimix_aimixapi.user.entity.User user) {
        try {
            // GPT 프롬프트 생성 (주관식 전용)
            String prompt = buildSubjectiveScoringPrompt(question, userAnswer);

            // GPT API 호출
            // 낮은 temperature(0.3)로 일관성 확보, 최대 토큰 수 증가(1500)로 상세한 피드백 제공
            String gptResponse = gptService.callGptApi(user, prompt, 0.3, 1500,
                    com.aimix_aimixapi.gpt.token.entity.GptUsageType.BATTLE_SCORING);

            // GPT 응답 파싱
            return parseScoringResponse(gptResponse);

        } catch (Exception e) {
            log.error("GPT 채점 실패, 기본 채점 로직 사용: {}", e.getMessage());
            // GPT 실패 시 기본 채점 로직 사용
            return basicScoring(question, userAnswer);
        }
    }

    /**
     * 객관식 답변 채점 (직접 비교)
     * 객관식 문제는 GPT를 사용하지 않고 직접 비교
     *
     * @param question   배틀 문제
     * @param userAnswer 사용자 답변 (정규화된 답변)
     * @return 채점 결과
     */
    public ScoringResult scoreObjectiveAnswer(BattleQuestion question, String userAnswer) {
        String correctAnswer = question.getCorrectAnswer().trim(); // 2
        String userAnswerTrimmed = userAnswer.trim();

        int score;
        String feedback;
        boolean isCorrect;

        // 정확히 일치하는 경우
        if (correctAnswer.equals(userAnswerTrimmed)) {
            score = 100;
            isCorrect = true;

            // 선택지 정보를 포함한 피드백 생성
            String correctChoiceText = getChoiceText(question, correctAnswer);
            if (correctChoiceText != null) {
                feedback = String.format("정답입니다! 완벽하게 맞추셨습니다.\n\n정답: %s. %s",
                        correctAnswer, correctChoiceText);
            } else {
                feedback = "정답입니다! 완벽하게 맞추셨습니다.";
            }
        }
        // 오답인 경우
        else {
            score = 0;
            isCorrect = false;

            // 정답과 오답 선택지 정보를 포함한 피드백 생성
            String correctChoiceText = getChoiceText(question, correctAnswer);
            String userChoiceText = getChoiceText(question, userAnswerTrimmed);

            StringBuilder feedbackBuilder = new StringBuilder();
            feedbackBuilder.append("사용자 답변은 정답과 일치하지 않습니다. ");

            if (correctChoiceText != null) {
                feedbackBuilder.append(String.format("정답은 '%s. %s' 입니다. ",
                        correctAnswer, correctChoiceText));
            } else {
                feedbackBuilder.append(String.format("정답은 '%s' 입니다. ", correctAnswer));
            }

            if (userChoiceText != null) {
                feedbackBuilder.append(String.format("선택하신 답변은 '%s. %s' 입니다. ",
                        userAnswerTrimmed, userChoiceText));
            }

            feedbackBuilder.append("다시 한번 생각해보시고 학습 내용을 복습해보세요.");
            feedback = feedbackBuilder.toString();
        }

        return ScoringResult.builder()
                .score(score)
                .feedback(feedback)
                .isCorrect(isCorrect)
                .build();
    }

    /**
     * 기본 채점 로직 (GPT 실패 시 사용)
     *
     * @param question   배틀 문제
     * @param userAnswer 사용자 답변
     * @return 채점 결과
     * @since 2025-12-18
     */
    private ScoringResult basicScoring(BattleQuestion question, String userAnswer) {
        String correctAnswer = question.getCorrectAnswer().trim().toLowerCase();
        String userAnswerLower = userAnswer.trim().toLowerCase();

        int score;
        String feedback;
        boolean isCorrect;

        // 정확히 일치하는 경우
        if (correctAnswer.equals(userAnswerLower)) {
            score = 100;
            feedback = "정답입니다! 완벽하게 맞추셨습니다.";
            isCorrect = true;
        }
        // 부분 일치하는 경우 (정답이 사용자 답변에 포함되거나, 사용자 답변이 정답에 포함되는 경우)
        else if (correctAnswer.contains(userAnswerLower) || userAnswerLower.contains(correctAnswer)) {
            score = 70;
            feedback = "부분 정답입니다. 정답: " + question.getCorrectAnswer() +
                    "\n답변에 핵심 내용이 포함되어 있지만 더 정확하게 작성하면 좋겠습니다.";
            isCorrect = false;
        }
        // 완전히 다른 경우
        else {
            score = 0;
            feedback = "오답입니다. 정답: " + question.getCorrectAnswer() +
                    "\n다시 한번 생각해보시고 학습 내용을 복습해보세요.";
            isCorrect = false;
        }

        return ScoringResult.builder()
                .score(score)
                .feedback(feedback)
                .isCorrect(isCorrect)
                .build();
    }

    /**
     * 주관식 문제 채점 프롬프트 생성
     * 주관식 문제에 대한 GPT 채점을 위한 프롬프트를 생성
     *
     * @param question   배틀 문제 (주관식)
     * @param userAnswer 사용자 답변
     * @return GPT 채점 프롬프트
     * @since 2025-12-18
     */
    private String buildSubjectiveScoringPrompt(BattleQuestion question, String userAnswer) {
        // Properties에서 템플릿 가져오기
        String template = battleProperties.getScoring().getSubjectivePrompt();

        return String.format(template,
                question.getQuestionText(),
                question.getCorrectAnswer(),
                userAnswer);
    }

    /**
     * GPT 응답 파싱
     *
     * @param response GPT API 응답 문자열
     * @return 채점 결과
     * @throws JsonProcessingException  JSON 파싱 실패 시
     * @throws IllegalArgumentException 응답이 비어있는 경우
     * @since 2025-12-18
     */
    private ScoringResult parseScoringResponse(String response) throws JsonProcessingException {
        // GPT 응답에서 JSON 추출 및 DTO로 파싱
        GptScoringResponse gptResponse = jsonUtils.fromGptResponse(response, GptScoringResponse.class);

        Integer score = gptResponse.getScore();
        String feedback = gptResponse.getFeedback();
        Boolean isCorrect = gptResponse.getIsCorrect();

        // 점수 유효성 검사
        if (score == null || score < 0 || score > 100) {
            log.warn("유효하지 않은 점수: {}, 기본값 0 사용", score);
            score = 0;
        }

        if (!StringUtils.hasText(feedback)) {
            feedback = "피드백을 생성할 수 없습니다.";
        }

        return ScoringResult.builder()
                .score(score)
                .feedback(feedback)
                .isCorrect(isCorrect != null && isCorrect)
                .build();
    }

    /**
     * 선택지 번호의 문항 텍스트 가져오기
     * <p>
     * 배틀 문제의 선택지 배열에서, 선택한 번호에 해당하는 텍스트를 반환
     * <p>
     * 예시:
     * - 선택지 배열: ["사과", "바나나", "오렌지"]
     * - 선택한 번호: "2"
     * - 반환값: "바나나" (2번째 선택지)
     *
     * @param question     배틀 문제 (선택지 배열이 JSON 문자열로 저장되어 있음)
     * @param choiceNumber 선택한 번호 (1-based, 예: "1", "2", "3")
     * @return 선택지 텍스트, 조회 실패 시 null
     * @since 2025-12-18
     */
    private String getChoiceText(BattleQuestion question, String choiceNumber) {
        // 선택지가 없는 경우
        if (question.getChoices() == null) {
            return null;
        }

        // JSON 문자열을 List로 파싱
        // question.getChoices() 예시
        // [
        // "여러 서버 간에 트래픽을 분산시키는 과정이다.",
        // "클라이언트의 요청을 처리하고 데이터를 제공하는 것이다.",
        // "소프트웨어 간의 상호작용을 위한 인터페이스를 만드는 것이다.",
        // "사용자의 신원을 확인하는 과정이다."
        // ]
        List<String> choices = parseChoicesFromJson(question.getChoices());
        if (CollectionUtils.isEmpty(choices)) {
            return null;
        }

        // 선택지 번호를 인덱스로 변환 (1-based → 0-based)
        int index = parseChoiceIndex(choiceNumber);
        if (index < 0 || index >= choices.size()) {
            return null;
        }

        return choices.get(index);
    }

    /**
     * JSON 문자열을 선택지 리스트로 파싱
     * <p>
     * 예시:
     * - 입력: "[\"선택지1\", \"선택지2\", \"선택지3\"]"
     * - 출력: ["선택지1", "선택지2", "선택지3"]
     *
     * @param choicesJson JSON 형태의 선택지 문자열
     * @return 파싱된 선택지 리스트, 실패 시 null
     * @since 2025-12-18
     */
    public List<String> parseChoicesFromJson(String choicesJson) {
        try {
            // TypeReference를 사용하여 List<String> 타입을 명시
            return jsonUtils.fromJson(choicesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("선택지 JSON 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 선택지 번호 문자열을 인덱스로 변환 (1-based → 0-based)
     *
     * @param choiceNumber 선택지 번호 문자열 (예: "1", "2")
     * @return 0-based 인덱스, 변환 실패 시 -1
     * @since 2025-12-18
     */
    private int parseChoiceIndex(String choiceNumber) {
        try {
            int number = Integer.parseInt(choiceNumber.trim());
            return number - 1; // 1-based → 0-based 변환
        } catch (NumberFormatException e) {
            return -1; // 숫자가 아닌 경우
        }
    }
}
