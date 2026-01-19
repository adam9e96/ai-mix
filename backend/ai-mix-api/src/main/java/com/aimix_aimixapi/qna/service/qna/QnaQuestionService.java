package com.aimix_aimixapi.qna.service.qna;

import com.aimix_aimixapi.common.exception.domain.QuestionNotFoundException;
import com.aimix_aimixapi.common.exception.domain.qna.AnonymousPasswordRequiredException;
import com.aimix_aimixapi.common.exception.domain.qna.LoginRequiredForNormalPostException;
import com.aimix_aimixapi.common.exception.message.ResourceMessage;
import com.aimix_aimixapi.qna.message.QnaMessage;
import com.aimix_aimixapi.qna.dto.qna.*;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.qna.mapper.QnaMapper;
import com.aimix_aimixapi.qna.repository.QnaAnswerRepository;
import com.aimix_aimixapi.qna.repository.QnaQuestionRepository;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * QNA 질문 서비스
 * - 질문 생성, 조회, 수정, 삭제
 * - 질문 상세 조회 (답변 목록 포함)
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class QnaQuestionService {

    private final QnaQuestionRepository questionRepository;
    private final QnaAnswerRepository answerRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final QnaAuthorizationService authorizationService;
    private final QnaMapper qnaMapper;

    /**
     * 질문 생성
     *
     * @param email   사용자 이메일 (인증된 사용자 식별용)
     * @param request 질문 생성 요청 DTO (제목, 내용, 익명 여부)
     * @return 생성된 질문 정보를 담은 응답 DTO
     * @throws AnonymousPasswordRequiredException 익명 게시글인데 비밀번호가 없는 경우
     * @throws LoginRequiredForNormalPostException 일반 게시글인데 로그인이 안 된 경우
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 익명 여부 결정 (null인 경우 기본값 false)
     * 2. 익명인 경우 비밀번호 암호화
     * 3. 사용자 정보 조회 (익명이 아닌 경우)
     * 4. 질문 엔티티 생성 및 저장
     * 5. 응답 DTO로 변환하여 반환
     * <p>
     * 주의사항:
     * - 익명으로 설정된 경우 user 필드는 null로 저장됨
     * - 제목과 내용은 요청 DTO에서 @NotBlank 검증을 통해 필수값으로 처리됨
     */
    @Transactional
    public QnaQuestionResponse createQuestion(String email, QnaQuestionCreateRequest request) {
        log.info("질문 생성 요청: email={}, title={}", email, request.getTitle());

        Boolean isAnonymous = determineIsAnonymous(request.getIsAnonymous());
        String encodedPassword = encodePasswordIfAnonymous(isAnonymous, request.getAnonymousPassword());
        User user = findUserForQuestion(email, isAnonymous);

        QnaQuestion question = buildQuestion(user, request, isAnonymous, encodedPassword);
        QnaQuestion savedQuestion = questionRepository.save(question);
        log.info("질문 저장 완료: questionId={}", savedQuestion.getId());

        return qnaMapper.toQuestionResponse(savedQuestion);
    }

    /**
     * 질문 목록 조회 (페이징)
     *
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 정보)
     * @return 페이징된 질문 목록 응답 (생성일 기준 내림차순 정렬)
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 데이터베이스에서 생성일 기준 내림차순으로 질문 목록 조회 (페이징 적용)
     * 2. 각 질문 엔티티를 QnaQuestionListResponse DTO로 변환
     * - 본문은 100자로 제한된 미리보기로 변환
     * - 태그, 작성자 정보, 답변 수, 조회수 등 포함
     * 3. 페이징 정보와 함께 반환
     */
    @Transactional(readOnly = true)
    public Page<QnaQuestionListResponse> getQuestionList(Pageable pageable) {
        log.info("질문 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<QnaQuestion> questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return questions.map(qnaMapper::toQuestionListResponse);
    }

    /**
     * 질문 상세 조회
     * - 질문 정보와 답변 목록을 함께 반환
     * - 조회 시 조회수 자동 증가
     *
     * @param questionId 조회할 질문 ID
     * @return 질문 상세 정보와 답변 목록을 포함한 응답 DTO
     * @throws QuestionNotFoundException 질문을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 질문 ID로 질문 엔티티 조회 (존재하지 않으면 QuestionNotFoundException 발생)
     * 2. 조회수 증가 (null인 경우 0으로 처리 후 1 증가)
     * 3. 해당 질문에 대한 답변 목록을 생성일 기준 오름차순으로 조회
     * 4. GPT 답변 존재 여부 확인
     * 5. 질문 정보와 답변 목록을 포함한 상세 응답 DTO 생성 및 반환
     * <p>
     * 주의사항:
     * - 질문 조회 시 조회수가 자동으로 증가함
     * - 답변 목록은 생성일 기준 오름차순으로 정렬됨
     */
    @Transactional
    public QnaQuestionDetailResponse getQuestionDetail(UUID questionId) {
        log.info("질문 상세 조회: questionId={}", questionId);

        QnaQuestion question = findQuestionById(questionId);
        question.incrementViewCount();
        questionRepository.save(question);

        List<QnaAnswer> answers = answerRepository.findByQuestionOrderByCreatedAtAsc(question);
        boolean hasGptAnswer = hasGptAnswer(answers);

        log.info("질문 상세 조회 완료: questionId={}, answerCount={}, hasGptAnswer={}",
                questionId, answers.size(), hasGptAnswer);

        return QnaQuestionDetailResponse.builder()
                .question(qnaMapper.toQuestionResponse(question))
                .answers(answers.stream()
                        .map(qnaMapper::toAnswerResponse)
                        .collect(Collectors.toList()))
                .hasGptAnswer(hasGptAnswer)
                .hasKnowledgeCard(false) // 순환 참조 방지를 위해 항상 false 반환 (프론트에서 별도 API로 확인)
                .build();
    }

    /**
     * 질문 수정
     * - 익명 게시글인 경우 비밀번호 검증 후 수정 가능
     * - 일반 게시글인 경우 작성자만 수정 가능
     *
     * @param email     사용자 이메일 (일반 게시글인 경우 필수)
     * @param questionId 수정할 질문 ID
     * @param request   질문 수정 요청 DTO
     * @return 수정된 질문 정보를 담은 응답 DTO
     * @throws QuestionNotFoundException 질문을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaQuestionResponse updateQuestion(String email, UUID questionId, QnaQuestionUpdateRequest request) {
        log.info("질문 수정 요청: email={}, questionId={}", email, questionId);

        QnaQuestion question = findQuestionById(questionId);
        authorizationService.validateQuestionUpdatePermission(question, email, request.getAnonymousPassword());

        User user = findUserIfEmailExists(email);
        updateQuestionFields(question, request, user);

        QnaQuestion savedQuestion = questionRepository.save(question);
        log.info("질문 수정 완료: questionId={}", savedQuestion.getId());

        return qnaMapper.toQuestionResponse(savedQuestion);
    }

    /**
     * 질문 삭제
     * - 익명 게시글인 경우 비밀번호 검증 필요
     * - 일반 게시글인 경우 작성자만 삭제 가능
     *
     * @param email             사용자 이메일 (일반 게시글인 경우 필수)
     * @param questionId        삭제할 질문 ID
     * @param anonymousPassword 익명 게시글 비밀번호 (익명 게시글인 경우 필수)
     * @throws QuestionNotFoundException 질문을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public void deleteQuestion(String email, UUID questionId, String anonymousPassword) {
        log.info("질문 삭제 요청: email={}, questionId={}", email, questionId);

        QnaQuestion question = findQuestionById(questionId);

        // 권한 검증
        authorizationService.validateQuestionDeletePermission(question, email, anonymousPassword);

        questionRepository.delete(question);
        log.info("질문 삭제 완료: questionId={}", questionId);
    }

    /**
     * 질문 ID로 질문 조회
     *
     * @param questionId 질문 ID
     * @return 조회된 질문 엔티티
     * @throws QuestionNotFoundException 질문을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional(readOnly = true)
    public QnaQuestion findQuestionById(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.warn("질문을 찾을 수 없습니다: questionId={}", questionId);
                    return new QuestionNotFoundException(ResourceMessage.QUESTION_NOT_FOUND.getMessage());
                });
    }

    // ========== 내부 헬퍼 메서드 ==========

    /**
     * 익명 여부 결정
     */
    private Boolean determineIsAnonymous(Boolean isAnonymous) {
        return isAnonymous != null ? isAnonymous : false;
    }

    /**
     * 익명인 경우 비밀번호 암호화
     *
     * @throws AnonymousPasswordRequiredException 익명인데 비밀번호가 없는 경우
     */
    private String encodePasswordIfAnonymous(Boolean isAnonymous, String password) {
        if (!isAnonymous) {
            return null;
        }
        if (!StringUtils.hasText(password)) {
            throw new AnonymousPasswordRequiredException(QnaMessage.ANONYMOUS_PASSWORD_REQUIRED.getMessage());
        }
        return passwordEncoder.encode(password);
    }

    /**
     * 질문 작성자를 위한 사용자 조회
     *
     * @throws LoginRequiredForNormalPostException 일반 게시글인데 로그인이 안 된 경우
     */
    private User findUserForQuestion(String email, Boolean isAnonymous) {
        if (isAnonymous) {
            return null;
        }
        if (email == null) {
            throw new LoginRequiredForNormalPostException(QnaMessage.LOGIN_REQUIRED_FOR_NORMAL_POST.getMessage());
        }
        return userService.findUserByEmail(email);
    }

    /**
     * 질문 엔티티 생성
     */
    private QnaQuestion buildQuestion(User user, QnaQuestionCreateRequest request,
                                      Boolean isAnonymous, String encodedPassword) {
        return QnaQuestion.builder()
                .user(user)
                .title(request.getTitle())
                .body(request.getBody())
                .isAnonymous(isAnonymous)
                .anonymousPassword(encodedPassword)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 이메일이 있으면 사용자 조회
     */
    private User findUserIfEmailExists(String email) {
        if (email == null) {
            return null;
        }
        return userService.findUserByEmail(email);
    }

    /**
     * 질문 필드 업데이트
     */
    private void updateQuestionFields(QnaQuestion question, QnaQuestionUpdateRequest request, User user) {
        question.updateTitle(request.getTitle());
        question.updateBody(request.getBody());
        updateQuestionAnonymousStatus(question, request.getIsAnonymous(), request.getAnonymousPassword(), user);
    }

    /**
     * 질문 익명 여부 업데이트
     */
    private void updateQuestionAnonymousStatus(QnaQuestion question, Boolean isAnonymous,
                                               String password, User user) {
        if (isAnonymous == null) {
            return;
        }

        if (isAnonymous) {
            changeToAnonymous(question, password);
        } else {
            changeToNormal(question, user);
        }
    }

    /**
     * 일반 게시글을 익명 게시글로 변경
     *
     * @throws AnonymousPasswordRequiredException 비밀번호가 필요한데 없는 경우
     */
    private void changeToAnonymous(QnaQuestion question, String password) {
        question.setIsAnonymous(true);
        question.setUser(null);
        if (StringUtils.hasText(password)) {
            question.setAnonymousPassword(passwordEncoder.encode(password));
        } else if (question.getAnonymousPassword() == null) {
            throw new AnonymousPasswordRequiredException(QnaMessage.PASSWORD_REQUIRED_FOR_ANONYMOUS.getMessage());
        }
    }

    /**
     * 익명 게시글을 일반 게시글로 변경
     *
     * @throws LoginRequiredForNormalPostException 로그인이 필요한데 사용자가 없는 경우
     */
    private void changeToNormal(QnaQuestion question, User user) {
        question.setIsAnonymous(false);
        if (user != null) {
            question.setUser(user);
            question.setAnonymousPassword(null);
        } else {
            throw new LoginRequiredForNormalPostException(QnaMessage.LOGIN_REQUIRED_FOR_NORMAL_POST_CHANGE.getMessage());
        }
    }

    /**
     * GPT 답변 존재 여부 확인
     */
    private boolean hasGptAnswer(List<QnaAnswer> answers) {
        return answers.stream()
                .anyMatch(QnaAnswer::isAiAnswer);
    }
}
