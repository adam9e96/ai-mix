package com.aimix_aimixapi.qna.service.qna;

import com.aimix_aimixapi.common.exception.domain.AnswerNotFoundException;
import com.aimix_aimixapi.common.exception.domain.user.UserNotFoundException;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaAnswerUpvote;
import com.aimix_aimixapi.qna.entity.VoteType;
import com.aimix_aimixapi.qna.repository.QnaAnswerRepository;
import com.aimix_aimixapi.qna.repository.QnaAnswerUpvoteRepository;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.repository.UserRepository;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * QNA 답변 추천/비추천 서비스
 * 투표 로직을 담당하는 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class QnaVoteService {

    private final QnaAnswerRepository answerRepository;
    private final QnaAnswerUpvoteRepository answerUpvoteRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 답변 추천 (upvote) - 토글 기능
     *
     * @param email    사용자 이메일
     * @param answerId 답변 ID
     * @return 업데이트된 답변
     */
    @Transactional
    public QnaAnswer upvoteAnswer(String email, UUID answerId) {
        log.info("답변 추천 요청: email={}, answerId={}", email, answerId);

        User user = userService.findUserByEmail(email);
        QnaAnswer answer = findAnswerById(answerId);
        var existingVote = answerUpvoteRepository.findByAnswerIdAndUserId(answerId, user.getId());

        if (existingVote.isPresent()) {
            handleExistingUpvote(existingVote.get(), answerId, user.getId());
        } else {
            createNewUpvote(answer, answerId, user.getId());
        }

        recalculateAnswerScore(answer);
        return answerRepository.save(answer);
    }

    /**
     * 답변 비추천 (downvote) - 토글 기능
     *
     * @param email    사용자 이메일
     * @param answerId 답변 ID
     * @return 업데이트된 답변
     */
    @Transactional
    public QnaAnswer downvoteAnswer(String email, UUID answerId) {
        log.info("답변 비추천 요청: email={}, answerId={}", email, answerId);

        User user = userService.findUserByEmail(email);
        QnaAnswer answer = findAnswerById(answerId);
        var existingVote = answerUpvoteRepository.findByAnswerIdAndUserId(answerId, user.getId());

        if (existingVote.isPresent()) {
            handleExistingDownvote(existingVote.get(), answerId, user.getId());
        } else {
            createNewDownvote(answer, answerId, user.getId());
        }

        recalculateAnswerScore(answer);
        return answerRepository.save(answer);
    }

    /**
     * 기존 추천 투표 처리
     */
    private void handleExistingUpvote(QnaAnswerUpvote vote, UUID answerId, Long userId) {
        if (vote.getVoteType() == VoteType.UP) {
            answerUpvoteRepository.deleteByAnswerIdAndUserId(answerId, userId);
            log.info("답변 추천 취소: answerId={}, userId={}", answerId, userId);
        } else {
            answerUpvoteRepository.deleteByAnswerIdAndUserId(answerId, userId);
            log.info("답변 비추천 취소 (0으로 복귀): answerId={}, userId={}", answerId, userId);
        }
    }

    /**
     * 기존 비추천 투표 처리
     */
    private void handleExistingDownvote(QnaAnswerUpvote vote, UUID answerId, Long userId) {
        if (vote.getVoteType() == VoteType.DOWN) {
            answerUpvoteRepository.deleteByAnswerIdAndUserId(answerId, userId);
            log.info("답변 비추천 취소: answerId={}, userId={}", answerId, userId);
        } else {
            answerUpvoteRepository.deleteByAnswerIdAndUserId(answerId, userId);
            log.info("답변 추천 취소 (0으로 복귀): answerId={}, userId={}", answerId, userId);
        }
    }

    /**
     * 새로운 추천 투표 생성
     */
    private void createNewUpvote(QnaAnswer answer, UUID answerId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        QnaAnswerUpvote upvote = QnaAnswerUpvote.builder()
                .answerId(answerId)
                .userId(userId)
                .voteType(VoteType.UP)
                .answer(answer)
                .user(user)
                .build();
        answerUpvoteRepository.save(upvote);
        log.info("답변 추천 추가: answerId={}, userId={}", answerId, userId);
    }

    /**
     * 새로운 비추천 투표 생성
     */
    private void createNewDownvote(QnaAnswer answer, UUID answerId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        QnaAnswerUpvote downvote = QnaAnswerUpvote.builder()
                .answerId(answerId)
                .userId(userId)
                .voteType(VoteType.DOWN)
                .answer(answer)
                .user(user)
                .build();
        answerUpvoteRepository.save(downvote);
        log.info("답변 비추천 추가: answerId={}, userId={}", answerId, userId);
    }

    /**
     * 답변 점수 재계산
     */
    private void recalculateAnswerScore(QnaAnswer answer) {
        long actualUpvote = answerUpvoteRepository.countByAnswerIdAndVoteType(answer.getId(), VoteType.UP);
        long actualDownvote = answerUpvoteRepository.countByAnswerIdAndVoteType(answer.getId(), VoteType.DOWN);
        int actualScore = (int) (actualUpvote - actualDownvote);
        answer.setScore(actualScore);
    }

    /**
     * 답변 ID로 답변 조회
     */
    private QnaAnswer findAnswerById(UUID answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> {
                    log.warn("답변을 찾을 수 없습니다: answerId={}", answerId);
                    return new AnswerNotFoundException(answerId);
                });
    }

    /**
     * 답변 삭제 시 관련 투표 기록 삭제
     */
    @Transactional
    public void deleteVotesByAnswerId(UUID answerId) {
        answerUpvoteRepository.deleteByAnswerId(answerId);
        log.info("답변 추천 기록 삭제 완료: answerId={}", answerId);
    }
}
