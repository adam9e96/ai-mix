package com.aimix_aimixapi.qna.service.battle;

import com.aimix_aimixapi.qna.config.QnaProperties;
import com.aimix_aimixapi.qna.entity.AnswerType;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.qna.entity.VoteType;
import com.aimix_aimixapi.qna.repository.QnaAnswerUpvoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 배틀 생성을 위한 QNA 데이터 수집 및 문자열 생성
 * 단일 책임: 배틀 데이터 문자열 생성
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class QnaBattleDataBuilder {

    private final QnaProperties qnaProperties;
    private final QnaAnswerUpvoteRepository answerUpvoteRepository;

    /**
     * 배틀 생성을 위한 QNA 데이터 문자열 생성
     * 선택된 답변과 GPT 답변을 모두 포함하여 생성합니다.
     * 사용자 답변이 있을 경우, GPT가 부족한 부분을 보완하도록 명시적으로 지시합니다.
     *
     * @param question        질문 엔티티
     * @param selectedAnswers 선택된 답변 목록
     * @param gptAnswers      GPT 답변 목록
     * @return 배틀 생성에 사용할 데이터 문자열
     */
    public String buildBattleDataString(QnaQuestion question, List<QnaAnswer> selectedAnswers,
                                        List<QnaAnswer> gptAnswers) {
        QnaProperties.BattleDataPrompt promptConfig = qnaProperties.getBattleDataPrompt();
        StringBuilder sb = new StringBuilder();

        // 질문 정보 추가
        appendQuestionInfo(sb, question, promptConfig);

        // 선택된 사용자 답변 추가
        List<QnaAnswer> userSelectedAnswers = filterUserAnswers(selectedAnswers);
        if (!userSelectedAnswers.isEmpty()) {
            appendUserSelectedAnswers(sb, userSelectedAnswers, promptConfig);
            sb.append(promptConfig.getUserAnswerSupplementInstruction());
        }

        // GPT 답변 추가
        if (!gptAnswers.isEmpty()) {
            appendGptAnswers(sb, gptAnswers, promptConfig);
        }

        return sb.toString();
    }

    private void appendQuestionInfo(StringBuilder sb, QnaQuestion question,
                                    QnaProperties.BattleDataPrompt promptConfig) {
        sb.append(promptConfig.getQuestionTitleLabel()).append(question.getTitle()).append("\n\n");
        sb.append(promptConfig.getQuestionBodyLabel()).append(question.getBody()).append("\n\n");
    }

    private List<QnaAnswer> filterUserAnswers(List<QnaAnswer> selectedAnswers) {
        return selectedAnswers.stream()
                .filter(answer -> answer.getAnswerType() == AnswerType.USER)
                .toList();
    }

    private void appendUserSelectedAnswers(StringBuilder sb, List<QnaAnswer> userSelectedAnswers,
                                           QnaProperties.BattleDataPrompt promptConfig) {
        sb.append(promptConfig.getSelectedUserAnswerLabel());
        for (int i = 0; i < userSelectedAnswers.size(); i++) {
            QnaAnswer answer = userSelectedAnswers.get(i);
            sb.append("[답변 ").append(i + 1).append("]");
            appendAnswerMetadata(sb, answer, promptConfig);
            sb.append("\n").append(answer.getBody());
            if (i < userSelectedAnswers.size() - 1) {
                sb.append("\n\n");
            }
        }
        sb.append("\n\n");
    }

    private void appendAnswerMetadata(StringBuilder sb, QnaAnswer answer,
                                      QnaProperties.BattleDataPrompt promptConfig) {
        if (Boolean.TRUE.equals(answer.getIsAccepted())) {
            sb.append(promptConfig.getAcceptedLabel());
        } else {
            long upvoteCount = answerUpvoteRepository.countByAnswerIdAndVoteType(answer.getId(), VoteType.UP);
            sb.append(String.format(promptConfig.getScoreFormat(), answer.getScore(), upvoteCount));
        }
    }

    private void appendGptAnswers(StringBuilder sb, List<QnaAnswer> gptAnswers,
                                  QnaProperties.BattleDataPrompt promptConfig) {
        sb.append(promptConfig.getGptAnswerLabel());
        for (int i = 0; i < gptAnswers.size(); i++) {
            QnaAnswer gptAnswer = gptAnswers.get(i);
            sb.append("[답변 ").append(i + 1).append("]\n");
            sb.append(gptAnswer.getBody());
            if (i < gptAnswers.size() - 1) {
                sb.append("\n\n");
            }
        }
    }
}
