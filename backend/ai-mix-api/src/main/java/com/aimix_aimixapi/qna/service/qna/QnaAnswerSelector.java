package com.aimix_aimixapi.qna.service.qna;

import com.aimix_aimixapi.qna.entity.AnswerType;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.VoteType;
import com.aimix_aimixapi.qna.repository.QnaAnswerUpvoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 배틀 생성을 위한 답변 선택 전략
 * 단일 책임: 우선순위에 따라 답변 선택
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class QnaAnswerSelector {

    private final QnaAnswerUpvoteRepository answerUpvoteRepository;

    /**
     * 배틀 생성을 위한 답변 선택
     * 우선순위: 1) 채택된 답변, 2) 양수 점수 답변 중 추천 수가 가장 높은 답변, 3) GPT 답변
     *
     * @param allAnswers 모든 답변 목록
     * @return 선택된 답변 목록
     */
    public List<QnaAnswer> selectAnswersForBattle(List<QnaAnswer> allAnswers) {
        // 1순위: 채택된 답변 찾기
        List<QnaAnswer> acceptedAnswers = findAcceptedAnswers(allAnswers);
        if (!acceptedAnswers.isEmpty()) {
            log.info("채택된 답변 선택: count={}", acceptedAnswers.size());
            return acceptedAnswers;
        }

        // 2순위: 양수 점수를 가진 답변 중 추천 수가 가장 높은 답변 찾기
        QnaAnswer bestPositiveScoreAnswer = findBestPositiveScoreAnswer(allAnswers);
        if (bestPositiveScoreAnswer != null) {
            long upvoteCount = answerUpvoteRepository.countByAnswerIdAndVoteType(
                    bestPositiveScoreAnswer.getId(), VoteType.UP);
            log.info("양수 점수 답변 중 추천 수가 가장 높은 답변 선택: answerId={}, score={}, upvoteCount={}",
                    bestPositiveScoreAnswer.getId(), bestPositiveScoreAnswer.getScore(), upvoteCount);
            return List.of(bestPositiveScoreAnswer);
        }

        // 3순위: GPT 답변 사용
        List<QnaAnswer> gptAnswers = findGptAnswers(allAnswers);
        log.info("GPT 답변 사용: count={}", gptAnswers.size());
        return gptAnswers;
    }

    private List<QnaAnswer> findAcceptedAnswers(List<QnaAnswer> allAnswers) {
        return allAnswers.stream()
                .filter(this::isAccepted)
                .toList();
    }

    private boolean isAccepted(QnaAnswer answer) {
        return Boolean.TRUE.equals(answer.getIsAccepted());
    }

    private QnaAnswer findBestPositiveScoreAnswer(List<QnaAnswer> allAnswers) {
        List<QnaAnswer> positiveScoreAnswers = allAnswers.stream()
                .filter(this::hasPositiveScore)
                .toList();

        if (positiveScoreAnswers.isEmpty()) {
            return null;
        }

        return positiveScoreAnswers.stream()
                .max(this::compareByUpvoteCount)
                .orElse(null);
    }

    private boolean hasPositiveScore(QnaAnswer answer) {
        return answer.getScore() != null && answer.getScore() > 0;
    }

    private int compareByUpvoteCount(QnaAnswer a1, QnaAnswer a2) {
        long upvote1 = answerUpvoteRepository.countByAnswerIdAndVoteType(a1.getId(), VoteType.UP);
        long upvote2 = answerUpvoteRepository.countByAnswerIdAndVoteType(a2.getId(), VoteType.UP);
        return Long.compare(upvote1, upvote2);
    }

    private List<QnaAnswer> findGptAnswers(List<QnaAnswer> allAnswers) {
        return allAnswers.stream()
                .filter(answer -> answer.getAnswerType() == AnswerType.AI)
                .toList();
    }
}
