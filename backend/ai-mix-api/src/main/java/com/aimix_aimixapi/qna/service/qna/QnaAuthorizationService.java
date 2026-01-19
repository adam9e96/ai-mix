package com.aimix_aimixapi.qna.service.qna;

import com.aimix_aimixapi.common.exception.domain.AccessDeniedException;
import com.aimix_aimixapi.common.exception.domain.qna.QnaPasswordMismatchException;
import com.aimix_aimixapi.qna.message.QnaMessage;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * QnA 권한 검증 서비스
 * - 질문/답변에 대한 권한 검증 로직을 담당
 * - 익명 게시글: 비밀번호 검증
 * - 일반 게시글: 작성자 확인
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class QnaAuthorizationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 질문 수정 권한 검증
     * - 익명 게시글: 비밀번호 검증
     * - 일반 게시글: 작성자 확인
     *
     * @param question           검증할 질문
     * @param email              사용자 이메일 (일반 게시글인 경우 필수)
     * @param anonymousPassword 익명 게시글 비밀번호 (익명 게시글인 경우 필수)
     * @throws AccessDeniedException 권한이 없는 경우
     */
    public void validateQuestionUpdatePermission(QnaQuestion question, String email, String anonymousPassword) {
        log.debug("질문 수정 권한 검증: questionId={}, isAnonymous={}",
                question.getId(), question.getIsAnonymous());

        if (isAnonymousQuestion(question)) {
            validateAnonymousPassword(question, anonymousPassword, "질문 수정");
        } else {
            validateQuestionAuthor(question, email, "질문 수정");
        }
    }

    /**
     * 질문 삭제 권한 검증
     * - 익명 게시글: 비밀번호 검증
     * - 일반 게시글: 작성자 확인
     *
     * @param question           검증할 질문
     * @param email              사용자 이메일 (일반 게시글인 경우 필수)
     * @param anonymousPassword 익명 게시글 비밀번호 (익명 게시글인 경우 필수)
     * @throws AccessDeniedException 권한이 없는 경우
     */
    public void validateQuestionDeletePermission(QnaQuestion question, String email, String anonymousPassword) {
        log.debug("질문 삭제 권한 검증: questionId={}, isAnonymous={}",
                question.getId(), question.getIsAnonymous());

        if (isAnonymousQuestion(question)) {
            validateAnonymousPassword(question, anonymousPassword, "질문 삭제");
        } else {
            validateQuestionAuthor(question, email, "질문 삭제");
        }
    }

    /**
     * 답변 채택 권한 검증
     * - 질문 작성자만 답변을 채택/해제할 수 있음
     * - 익명 질문: 비밀번호 검증
     * - 일반 질문: 질문 작성자 확인
     *
     * @param question           답변이 속한 질문
     * @param email              사용자 이메일 (일반 질문인 경우 필수)
     * @param anonymousPassword 익명 질문 비밀번호 (익명 질문인 경우 필수)
     * @throws AccessDeniedException 권한이 없는 경우
     */
    public void validateAnswerAcceptPermission(QnaQuestion question, String email, String anonymousPassword) {
        log.debug("답변 채택 권한 검증: questionId={}, isAnonymous={}",
                question.getId(), question.getIsAnonymous());

        if (isAnonymousQuestion(question)) {
            validateAnonymousPassword(question, anonymousPassword, "답변 채택/해제");
        } else {
            validateQuestionAuthor(question, email, "답변 채택/해제");
        }
    }

    /**
     * 답변 수정/삭제 권한 검증
     * - 답변 작성자만 수정/삭제 가능
     * - AI 답변은 수정 불가
     *
     * @param answer 검증할 답변
     * @param email 사용자 이메일
     * @throws AccessDeniedException 권한이 없는 경우
     */
    public void validateAnswerModifyPermission(QnaAnswer answer, String email) {
        log.debug("답변 수정/삭제 권한 검증: answerId={}, answerType={}",
                answer.getId(), answer.getAnswerType());

        if (answer.getUser() == null) {
            throw new AccessDeniedException(QnaMessage.ANSWER_NO_PERMISSION.getMessage());
        }

        if (email == null) {
            throw new AccessDeniedException(QnaMessage.ANSWER_MODIFY_LOGIN_REQUIRED.getMessage());
        }

        User user = userService.findUserByEmail(email);
        if (!answer.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(QnaMessage.ANSWER_NO_PERMISSION.getMessage());
        }
    }

    /**
     * 익명 질문 여부 확인
     *
     * @param question 질문
     * @return 익명 질문이면 true
     */
    private boolean isAnonymousQuestion(QnaQuestion question) {
        return Boolean.TRUE.equals(question.getIsAnonymous()) || question.getUser() == null;
    }

    /**
     * 익명 게시글 비밀번호 검증
     *
     * @param question           질문
     * @param anonymousPassword 입력된 비밀번호
     * @param action             수행하려는 작업 (에러 메시지에 사용)
     * @throws AccessDeniedException 비밀번호가 없거나 일치하지 않는 경우
     */
    private void validateAnonymousPassword(QnaQuestion question, String anonymousPassword, String action) {
        if (!StringUtils.hasText(anonymousPassword)) {
            throw new AccessDeniedException(
                    QnaMessage.ANONYMOUS_PASSWORD_REQUIRED_FOR_ACTION.format(action));
        }

        if (question.getAnonymousPassword() == null ||
                !passwordEncoder.matches(anonymousPassword, question.getAnonymousPassword())) {
            throw new QnaPasswordMismatchException(QnaMessage.PASSWORD_MISMATCH.getMessage());
        }
    }

    /**
     * 질문 작성자 확인
     *
     * @param question 질문
     * @param email    사용자 이메일
     * @param action   수행하려는 작업 (에러 메시지에 사용)
     * @throws AccessDeniedException 로그인이 없거나 작성자가 아닌 경우
     */
    private void validateQuestionAuthor(QnaQuestion question, String email, String action) {
        if (email == null) {
            throw new AccessDeniedException(
                    QnaMessage.LOGIN_REQUIRED_FOR_ACTION.format(action));
        }

        User user = userService.findUserByEmail(email);
        if (!question.getUser().getId().equals(user.getId())) {
            if (action.contains("채택")) {
                throw new AccessDeniedException(QnaMessage.ONLY_QUESTION_AUTHOR_CAN_ACCEPT.getMessage());
            } else {
                throw new AccessDeniedException(
                        QnaMessage.NO_PERMISSION_FOR_ACTION.format(action));
            }
        }
    }
}