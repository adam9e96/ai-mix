package com.aimix_aimixapi.qna.service.battle;

import com.aimix_aimixapi.qna.config.QnaProperties;
import com.aimix_aimixapi.qna.entity.AnswerType;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * QNA GPT 답변 생성을 위한 프롬프트 빌더
 * 단일 책임: GPT 프롬프트 문자열 생성
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class QnaGptPromptBuilder {

    private final QnaProperties qnaProperties;

    /**
     * QNA 답변 생성을 위한 GPT 프롬프트 생성
     *
     * @param question        답변을 생성할 질문 엔티티
     * @param existingAnswers 기존 답변 목록 (참고용, null이거나 비어있을 수 있음)
     * @return GPT API에 전달할 프롬프트 문자열
     */
    public String buildAnswerPrompt(QnaQuestion question, List<QnaAnswer> existingAnswers) {
        QnaProperties.GptPrompt promptConfig = qnaProperties.getGptPrompt();
        StringBuilder prompt = new StringBuilder();

        // 1. 질문 제목과 내용 추가
        prompt.append(promptConfig.getIntroduction());
        prompt.append(promptConfig.getQuestionTitleLabel()).append(question.getTitle()).append("\n\n");
        prompt.append(promptConfig.getQuestionBodyLabel()).append(question.getBody()).append("\n\n");

        // 2. 기존 답변이 있으면 참고용으로 추가
        if (hasExistingAnswers(existingAnswers)) {
            appendExistingAnswers(prompt, existingAnswers, promptConfig);
        }

        // 3. 답변 작성 가이드라인 추가
        prompt.append(promptConfig.getGuidelines());

        // 4. 마크다운 형식 지시사항 추가
        prompt.append(promptConfig.getMarkdownFormat());

        // 5. 답변 시작 라벨
        prompt.append(promptConfig.getAnswerLabel());

        return prompt.toString();
    }

    private boolean hasExistingAnswers(List<QnaAnswer> existingAnswers) {
        return existingAnswers != null && !existingAnswers.isEmpty();
    }

    private void appendExistingAnswers(StringBuilder prompt, List<QnaAnswer> existingAnswers,
                                       QnaProperties.GptPrompt promptConfig) {
        prompt.append(promptConfig.getExistingAnswersLabel());
        for (int i = 0; i < existingAnswers.size(); i++) {
            QnaAnswer existingAnswer = existingAnswers.get(i);
            String answerTypeLabel = existingAnswer.getAnswerType() == AnswerType.AI ? "[AI]" : "[사용자]";
            prompt.append(i + 1).append(". ").append(answerTypeLabel).append(": ")
                    .append(existingAnswer.getBody()).append("\n\n");
        }
        prompt.append(promptConfig.getExistingAnswersInstruction());
    }

    /**
     * QNA 태그 생성을 위한 GPT 프롬프트 생성
     * 질문의 제목과 내용을 기반으로 적절한 태그를 추천받기 위한 프롬프트를 생성합니다.
     *
     * @param question 태그를 생성할 질문 엔티티
     * @return GPT API에 전달할 프롬프트 문자열
     */
    public String buildTagPrompt(QnaQuestion question) {
        QnaProperties.TagPrompt promptConfig = qnaProperties.getTagPrompt();
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(promptConfig.getIntroduction());
        prompt.append(promptConfig.getTitleLabel()).append(question.getTitle()).append("\n\n");
        prompt.append(promptConfig.getBodyLabel()).append(question.getBody()).append("\n\n");
        prompt.append(promptConfig.getConditions());
        prompt.append(promptConfig.getResponseFormat());
        prompt.append(promptConfig.getInstruction());
        
        return prompt.toString();
    }
}
