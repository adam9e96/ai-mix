package com.aimix_aimixapi.battle.service;

import com.aimix_aimixapi.battle.config.BattleProperties;
import com.aimix_aimixapi.battle.dto.GptQuestionResponse;
import com.aimix_aimixapi.battle.dto.QuestionData;
import com.aimix_aimixapi.battle.entity.QuestionType;
import com.aimix_aimixapi.gpt.dto.GptMessage;
import com.aimix_aimixapi.gpt.dto.GptMessageRole;
import com.aimix_aimixapi.gpt.service.GptService;
import com.aimix_aimixapi.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 배틀 문제 생성 서비스
 * - GPT를 이용한 문제 생성
 * - 대화 내용 또는 QnA 데이터를 분석하여 문제 생성
 * 
 * @since 2025-12-18
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class BattleQuestionGenerationService {

    private final GptService gptService;
    private final JsonUtils jsonUtils;
    private final BattleProperties battleProperties;

    /**
     * ChatGPT API를 호출하여 대화 내용에서 문제 생성
     *
     * @param conversationText 대화 내용 또는 QnA 데이터 텍스트
     * @param questionType     문제 유형 (주관식/객관식)
     * @param user             사용자 (토큰 사용량 기록용, null 가능)
     * @return 생성된 문제 목록
     * @since 2025-12-18
     */
    public List<QuestionData> generateQuestionsFromConversation(String conversationText, QuestionType questionType,
            com.aimix_aimixapi.user.entity.User user) {
        // 입력 검증
        if (!StringUtils.hasText(conversationText)) {
            log.warn("대화 내용이 비어있습니다");
            throw new IllegalArgumentException("대화 내용이 비어있습니다. 문제를 생성할 수 없습니다.");
        }

        if (questionType == null) {
            log.warn("문제 유형이 지정되지 않았습니다");
            throw new IllegalArgumentException("문제 유형(주관식/객관식)이 지정되지 않았습니다.");
        }

        try {
            // System Message: 지침 및 역할 정의
            String systemMessage = buildQuestionGenerationSystemMessage(questionType);

            // User Message: 대화 내용
            String userMessage = String.format("다음 대화 내용을 분석하여 문제를 생성해주세요:\n\n%s", conversationText);

            log.info("GPT API 호출하여 문제 생성: systemMessageLength={}, userMessageLength={}",
                    systemMessage.length(), userMessage.length());

            // GptService를 통해 GPT API 호출 (System Message + User Message)
            List<GptMessage> messages = List.of(
                    GptMessage.builder()
                            .role(GptMessageRole.SYSTEM) // System message로 지침 전달
                            .content(systemMessage)
                            .build(),
                    GptMessage.builder()
                            .role(GptMessageRole.USER)
                            .content(userMessage)
                            .build());

            // 설정값 사용
            int maxTokens = battleProperties.getQuestionGenerationMaxTokens();
            double temperature = battleProperties.getQuestionGenerationTemperature();

            String response = gptService.callGptApiWithMessages(user, messages, temperature, maxTokens,
                    com.aimix_aimixapi.gpt.token.entity.GptUsageType.BATTLE_QUESTION);

            log.info("GPT API 응답 수신: responseLength={}", response.length());

            // JSON 응답 파싱
            return parseQuestionsFromResponse(response);

        } catch (Exception e) {
            log.error("GPT API 호출 실패", e);
            throw new RuntimeException("문제 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 문제 생성 System Message 생성 (지침 및 역할 정의)
     * 
     * @since 2025-12-18
     */
    private String buildQuestionGenerationSystemMessage(QuestionType questionType) {

        String questionTypeDescription;
        String jsonFormat;
        String typeSpecificRequirements;

        if (questionType == QuestionType.OBJECTIVE) {
            questionTypeDescription = "객관식";
            jsonFormat = battleProperties.getQuestionGeneration().getObjectiveJsonFormat();
            typeSpecificRequirements = battleProperties.getQuestionGeneration().getObjectiveRequirements();
        } else {
            questionTypeDescription = "주관식";
            jsonFormat = battleProperties.getQuestionGeneration().getSubjectiveJsonFormat();
            typeSpecificRequirements = battleProperties.getQuestionGeneration().getSubjectiveRequirements();
        }

        // 템플릿에 값 주입
        return String.format(battleProperties.getQuestionGeneration().getSystemMessageTemplate(),
                questionTypeDescription,
                jsonFormat,
                typeSpecificRequirements);
    }

    /**
     * ChatGPT 응답에서 문제 목록 파싱
     * 
     * @param response GPT API 응답 문자열
     * @return 검증된 문제 목록
     * @since 2025-12-18
     */
    private List<QuestionData> parseQuestionsFromResponse(String response) {
        try {
            // GPT 응답에서 JSON 추출 및 DTO로 파싱
            GptQuestionResponse gptResponse = jsonUtils.fromGptResponse(response, GptQuestionResponse.class);

            if (CollectionUtils.isEmpty(gptResponse.getQuestions())) {
                log.warn("응답에 questions 필드가 없거나 비어있습니다");
                return new ArrayList<>();
            }

            List<QuestionData> questions = new ArrayList<>();
            for (GptQuestionResponse.QuestionItem item : gptResponse.getQuestions()) {
                if (item.getQuestionText() != null && item.getCorrectAnswer() != null) {
                    // questionType 파싱 (enum으로 자동 변환됨, null인 경우 기본값 사용)
                    QuestionType parsedQuestionType = item.getQuestionType() != null
                            ? item.getQuestionType()
                            : QuestionType.SUBJECTIVE;

                    // 객관식 문제의 경우 선택지 검증
                    if (parsedQuestionType == QuestionType.OBJECTIVE) {
                        if (CollectionUtils.isEmpty(item.getChoices()) || item.getChoices().size() != 4) {
                            log.warn("객관식 문제의 선택지가 4개가 아닙니다: choicesCount={}, questionText={}",
                                    item.getChoices() != null ? item.getChoices().size() : 0,
                                    item.getQuestionText());
                            // 선택지가 올바르지 않으면 해당 문제를 건너뜀
                            continue;
                        }
                    }

                    questions.add(QuestionData.builder()
                            .questionText(item.getQuestionText())
                            .correctAnswer(item.getCorrectAnswer())
                            .difficulty(item.getDifficulty() != null ? item.getDifficulty() : "MEDIUM") // 기본값 MEDIUM
                            .questionType(parsedQuestionType)
                            .choices(item.getChoices()) // 객관식인 경우 선택지, 주관식인 경우 null
                            .build());
                }
            }

            return questions;

        } catch (Exception e) {
            log.error("문제 파싱 실패: response={}", response, e);
            throw new RuntimeException("문제 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

}
