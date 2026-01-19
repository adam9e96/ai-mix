package com.aimix_aimixapi.qna.service.qna;

import com.aimix_aimixapi.common.exception.domain.AnswerNotFoundException;
import com.aimix_aimixapi.common.exception.domain.QuestionNotFoundException;
import com.aimix_aimixapi.common.exception.domain.qna.AiAnswerCannotBeModifiedException;
import com.aimix_aimixapi.common.exception.message.ResourceMessage;
import com.aimix_aimixapi.qna.message.QnaMessage;
import com.aimix_aimixapi.gpt.service.GptService;
import com.aimix_aimixapi.qna.dto.qna.QnaAnswerCreateRequest;
import com.aimix_aimixapi.qna.dto.qna.QnaAnswerResponse;
import com.aimix_aimixapi.qna.dto.qna.QnaAnswerUpdateRequest;
import com.aimix_aimixapi.qna.entity.AnswerType;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.qna.mapper.QnaMapper;
import com.aimix_aimixapi.qna.repository.QnaAnswerRepository;
import com.aimix_aimixapi.qna.service.battle.QnaGptPromptBuilder;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * QNA 답변 서비스
 * - 답변 생성, 수정, 삭제
 * - AI 답변 생성
 * - 답변 채택/해제
 * - 답변 추천/비추천
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class QnaAnswerService {

    private final QnaAnswerRepository answerRepository;
    private final QnaQuestionService questionService;
    private final UserService userService;
    private final QnaAuthorizationService authorizationService;
    private final QnaMapper qnaMapper;
    private final QnaVoteService voteService;
    private final QnaGptPromptBuilder gptPromptBuilder;
    private final GptService gptService;

    /**
     * 답변 생성
     *
     * @param email   사용자 이메일 (인증된 사용자 식별용)
     * @param request 답변 생성 요청 DTO (질문 ID, 답변 내용, 답변 타입)
     * @return 생성된 답변 정보를 담은 응답 DTO
     * @throws QuestionNotFoundException 질문을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 이메일을 통해 사용자 정보 조회
     * 2. 질문 ID로 질문 엔티티 조회
     * 3. 답변 타입 확인 및 설정
     * 4. 답변 엔티티 생성 및 저장
     * 5. 질문 엔티티에 답변 추가 (양방향 연관관계 설정)
     * 6. 저장된 답변을 응답 DTO로 변환하여 반환
     * <p>
     * 주의사항:
     * - AI 답변인 경우 user 필드는 null로 저장됨
     * - 답변 내용은 요청 DTO에서 필수값으로 처리됨
     */
    @Transactional
    public QnaAnswerResponse createAnswer(String email, QnaAnswerCreateRequest request) {
        log.info("답변 생성 요청: email={}, questionId={}", email, request.getQuestionId());

        User user = userService.findUserByEmail(email);
        QnaQuestion question = questionService.findQuestionById(request.getQuestionId());
        AnswerType answerType = determineAnswerType(request.getAnswerType());

        QnaAnswer answer = buildAnswer(question, user, answerType, request.getBody());
        QnaAnswer savedAnswer = answerRepository.save(answer);
        question.addAnswer(savedAnswer);
        log.info("답변 저장 완료: answerId={}", savedAnswer.getId());

        return qnaMapper.toAnswerResponse(savedAnswer);
    }

    /**
     * 답변 수정
     * - 답변 작성자만 수정 가능
     * - AI 답변은 수정 불가
     *
     * @param email    사용자 이메일
     * @param answerId 수정할 답변 ID
     * @param request  답변 수정 요청 DTO
     * @return 수정된 답변 정보를 담은 응답 DTO
     * @throws AnswerNotFoundException 답변을 찾을 수 없는 경우
     * @throws AiAnswerCannotBeModifiedException AI 답변은 수정할 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse updateAnswer(String email, UUID answerId, QnaAnswerUpdateRequest request) {
        log.info("답변 수정 요청: email={}, answerId={}", email, answerId);

        QnaAnswer answer = findAnswerById(answerId);
        validateAnswerCanBeModified(answer);
        authorizationService.validateAnswerModifyPermission(answer, email);

        updateAnswerBody(answer, request.getBody());

        QnaAnswer savedAnswer = answerRepository.save(answer);
        log.info("답변 수정 완료: answerId={}", savedAnswer.getId());

        return qnaMapper.toAnswerResponse(savedAnswer);
    }

    /**
     * 답변 삭제
     * - 답변 작성자만 삭제 가능
     * - 삭제 시 관련 추천 기록도 함께 삭제
     *
     * @param email    사용자 이메일
     * @param answerId 삭제할 답변 ID
     * @throws AnswerNotFoundException 답변을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public void deleteAnswer(String email, UUID answerId) {
        log.info("답변 삭제 요청: email={}, answerId={}", email, answerId);

        QnaAnswer answer = findAnswerById(answerId);

        // 권한 검증
        authorizationService.validateAnswerModifyPermission(answer, email);

        // 답변 삭제 전에 관련 추천 기록도 함께 삭제
        voteService.deleteVotesByAnswerId(answerId);

        answerRepository.delete(answer);
        log.info("답변 삭제 완료: answerId={}", answerId);
    }

    /**
     * GPT로 AI 답변 생성
     * - 질문과 기존 답변을 참고하여 GPT API를 통해 AI 답변 생성
     * - 요청한 사용자(현재 로그인 사용자)의 API 키 사용
     * - 토큰 사용량을 요청한 사용자에게 기록 (사용자가 null인 경우 기록하지 않음)
     *
     * @param user 요청한 사용자 (현재 로그인 사용자, null 가능)
     * @param questionId AI 답변을 생성할 질문 ID
     * @return 생성된 AI 답변 정보를 담은 응답 DTO
     * @throws QuestionNotFoundException 질문을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     * <p>
     * 동작 과정:
     * 1. 질문 ID로 질문 엔티티 조회
     * 2. 해당 질문에 대한 기존 답변 목록 조회 (생성일 기준 오름차순)
     * 3. GPT 프롬프트 생성 (질문 제목, 내용, 기존 답변 포함)
     * 4. GPT API 호출하여 AI 답변 생성 (요청한 사용자 정보로 토큰 사용량 기록)
     * 5. AI 답변 엔티티 생성 및 저장
     * 6. 질문 엔티티에 답변 추가 (양방향 연관관계 설정)
     * 7. 저장된 AI 답변을 응답 DTO로 변환하여 반환
     * <p>
     * 주의사항:
     * - GPT API 호출 실패 시 예외가 발생할 수 있음
     * - AI 답변은 user 필드가 null이며, answerType이 AI로 설정됨
     * - 기존 답변이 있으면 이를 참고하여 더 나은 답변을 생성함
     * - 사용자가 null인 경우 토큰 사용량을 기록하지 않음
     */
    @Transactional
    public QnaAnswerResponse createAiAnswer(User user, UUID questionId) {
        log.info("GPT 답변 생성 요청: questionId={}, userId={}", 
                questionId, user != null ? user.getId() : null);

        QnaQuestion question = questionService.findQuestionById(questionId);
        List<QnaAnswer> existingAnswers = answerRepository.findByQuestionOrderByCreatedAtAsc(question);

        String prompt = gptPromptBuilder.buildAnswerPrompt(question, existingAnswers);
        log.info("AI가 입력받은 prompt: {}", prompt);

        // 요청한 사용자(현재 로그인 사용자)의 API 키 사용
        // 사용자가 null인 경우 토큰 사용량을 기록하지 않음
        String aiResponse = user != null
                ? gptService.callGptApi(user, prompt, null, null, 
                        com.aimix_aimixapi.gpt.token.entity.GptUsageType.QNA)
                : gptService.callGptApi(prompt);
        
        QnaAnswer answer = buildAiAnswer(question, aiResponse);

        QnaAnswer savedAnswer = answerRepository.save(answer);
        question.addAnswer(savedAnswer);
        log.info("GPT 답변 저장 완료: answerId={}", savedAnswer.getId());

        return qnaMapper.toAnswerResponse(savedAnswer);
    }

    /**
     * 답변 채택/해제
     * - 질문 작성자만 수행 가능
     * - 익명 질문인 경우 비밀번호로 권한 검증
     * - 한 질문당 최대 1개의 답변만 채택 가능
     * - 이미 채택된 답변을 다시 호출하면 채택 해제 (토글)
     *
     * @param email             요청 사용자 이메일 (로그인 사용자)
     * @param answerId          채택/해제할 답변 ID
     * @param anonymousPassword 익명 질문 비밀번호 (익명 질문인 경우 필수)
     * @return 업데이트된 답변 정보를 담은 응답 DTO
     * @throws AnswerNotFoundException 답변을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse acceptAnswer(String email, UUID answerId, String anonymousPassword) {
        log.info("답변 채택/해제 요청: email={}, answerId={}", email, answerId);

        QnaAnswer answer = findAnswerById(answerId);
        QnaQuestion question = answer.getQuestion();
        authorizationService.validateAnswerAcceptPermission(question, email, anonymousPassword);

        boolean willBeAccepted = answer.toggleAcceptStatus();
        if (willBeAccepted) {
            unacceptOtherAnswers(question, answer.getId());
        }

        QnaAnswer savedAnswer = answerRepository.save(answer);
        log.info("답변 채택/해제 처리 완료: answerId={}, isAccepted={}",
                savedAnswer.getId(), savedAnswer.getIsAccepted());

        return qnaMapper.toAnswerResponse(savedAnswer);
    }

    /**
     * 답변 추천 (upvote) - 토글 기능
     * - 로그인한 사용자만 추천 가능
     * - Stack Overflow 스타일: 같은 타입을 다시 누르면 취소, 다른 타입을 누르면 전환
     *
     * @param email    사용자 이메일 (인증된 사용자 식별용)
     * @param answerId 답변 ID
     * @return 업데이트된 답변 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse upvoteAnswer(String email, UUID answerId) {
        log.info("답변 추천 요청: email={}, answerId={}", email, answerId);

        QnaAnswer answer = voteService.upvoteAnswer(email, answerId);
        return qnaMapper.toAnswerResponse(answer);
    }

    /**
     * 답변 비추천 (downvote) - 토글 기능
     * - 로그인한 사용자만 비추천 가능
     * - Stack Overflow 스타일: 같은 타입을 다시 누르면 취소, 다른 타입을 누르면 전환
     *
     * @param email    사용자 이메일 (인증된 사용자 식별용)
     * @param answerId 답변 ID
     * @return 업데이트된 답변 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse downvoteAnswer(String email, UUID answerId) {
        log.info("답변 비추천 요청: email={}, answerId={}", email, answerId);

        QnaAnswer answer = voteService.downvoteAnswer(email, answerId);
        return qnaMapper.toAnswerResponse(answer);
    }

    /**
     * 답변 ID로 답변 조회
     *
     * @param answerId 답변 ID
     * @return 조회된 답변 엔티티
     * @throws AnswerNotFoundException 답변을 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional(readOnly = true)
    public QnaAnswer findAnswerById(UUID answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> {
                    log.warn("답변을 찾을 수 없습니다: answerId={}", answerId);
                    return new AnswerNotFoundException(ResourceMessage.ANSWER_NOT_FOUND.getMessage());
                });
    }

    // ========== 내부 헬퍼 메서드 ==========

    /**
     * 답변 타입 결정
     */
    private AnswerType determineAnswerType(String answerType) {
        return "AI".equalsIgnoreCase(answerType) ? AnswerType.AI : AnswerType.USER;
    }

    /**
     * 답변 엔티티 생성
     */
    private QnaAnswer buildAnswer(QnaQuestion question, User user, AnswerType answerType, String body) {
        return QnaAnswer.builder()
                .question(question)
                .user(answerType == AnswerType.USER ? user : null)
                .answerType(answerType)
                .body(body)
                .score(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * AI 답변 수정 가능 여부 검증
     *
     * @throws AiAnswerCannotBeModifiedException AI 답변은 수정할 수 없는 경우
     */
    private void validateAnswerCanBeModified(QnaAnswer answer) {
        if (answer.isAiAnswer()) {
            throw new AiAnswerCannotBeModifiedException(QnaMessage.AI_ANSWER_CANNOT_BE_MODIFIED.getMessage());
        }
    }

    /**
     * 답변 내용 업데이트
     */
    private void updateAnswerBody(QnaAnswer answer, String body) {
        answer.updateBody(body);
    }

    /**
     * AI 답변 엔티티 생성
     */
    private QnaAnswer buildAiAnswer(QnaQuestion question, String aiResponse) {
        return QnaAnswer.builder()
                .question(question)
                .user(null)
                .answerType(AnswerType.AI)
                .body(aiResponse)
                .score(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 다른 답변들의 채택 상태 해제
     */
    private void unacceptOtherAnswers(QnaQuestion question, UUID currentAnswerId) {
        List<QnaAnswer> answersForQuestion = answerRepository.findByQuestionOrderByCreatedAtAsc(question);
        for (QnaAnswer other : answersForQuestion) {
            if (!other.getId().equals(currentAnswerId) && other.isAccepted()) {
                other.unaccept();
                answerRepository.save(other);
                log.info("다른 답변 채택 해제: answerId={}", other.getId());
            }
        }
    }
}
