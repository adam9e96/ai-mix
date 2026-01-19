package com.aimix_aimixapi.qna.service.qna;

import com.aimix_aimixapi.common.exception.domain.qna.GptAnswerNotFoundException;
import com.aimix_aimixapi.qna.message.QnaMessage;
import com.aimix_aimixapi.qna.dto.qna.*;
import com.aimix_aimixapi.qna.dto.tag.QnaTagGenerateRequest;
import com.aimix_aimixapi.qna.dto.tag.QnaTagGenerateResponse;
import com.aimix_aimixapi.qna.entity.QnaAnswer;
import com.aimix_aimixapi.qna.entity.QnaQuestion;
import com.aimix_aimixapi.qna.repository.QnaAnswerRepository;
import com.aimix_aimixapi.qna.service.battle.QnaBattleDataBuilder;
import com.aimix_aimixapi.qna.service.tag.QnaTagService;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * QNA 서비스 (Facade)
 * - 질문/답변/검색/태그 서비스를 조합하여 제공
 * - 배틀 데이터 수집 기능 제공
 * - 단일 진입점 역할
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaQuestionService questionService;
    private final QnaAnswerService answerService;
    private final QnaSearchService searchService;
    private final QnaTagService tagService;
    private final QnaAnswerRepository answerRepository;
    private final QnaAnswerSelector answerSelector;
    private final QnaBattleDataBuilder battleDataBuilder;

    // ========== 질문 관련 메서드 ==========

    /**
     * 질문 생성
     *
     * @param email   사용자 이메일
     * @param request 질문 생성 요청 DTO
     * @return 생성된 질문 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaQuestionResponse createQuestion(String email, QnaQuestionCreateRequest request) {
        return questionService.createQuestion(email, request);
    }

    /**
     * 질문 목록 조회 (페이징)
     *
     * @param pageable 페이징 정보
     * @return 페이징된 질문 목록 응답
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional(readOnly = true)
    public Page<QnaQuestionListResponse> getQuestionList(Pageable pageable) {
        return questionService.getQuestionList(pageable);
    }

    /**
     * 질문 검색
     *
     * @param keyword    검색어
     * @param searchType 검색 타입
     * @param pageable   페이징 정보
     * @return 페이징된 질문 목록 응답
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional(readOnly = true)
    public Page<QnaQuestionListResponse> searchQuestions(String keyword, String searchType, Pageable pageable) {
        return searchService.searchQuestions(keyword, searchType, pageable);
    }

    /**
     * 질문 상세 조회
     *
     * @param questionId 조회할 질문 ID
     * @return 질문 상세 정보와 답변 목록을 포함한 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaQuestionDetailResponse getQuestionDetail(UUID questionId) {
        return questionService.getQuestionDetail(questionId);
    }

    /**
     * 질문 수정
     *
     * @param email     사용자 이메일
     * @param questionId 수정할 질문 ID
     * @param request   질문 수정 요청 DTO
     * @return 수정된 질문 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaQuestionResponse updateQuestion(String email, UUID questionId, QnaQuestionUpdateRequest request) {
        return questionService.updateQuestion(email, questionId, request);
    }

    /**
     * 질문 삭제
     *
     * @param email             사용자 이메일
     * @param questionId        삭제할 질문 ID
     * @param anonymousPassword 익명 게시글 비밀번호
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public void deleteQuestion(String email, UUID questionId, String anonymousPassword) {
        questionService.deleteQuestion(email, questionId, anonymousPassword);
    }

    // ========== 답변 관련 메서드 ==========

    /**
     * 답변 생성
     *
     * @param email   사용자 이메일
     * @param request 답변 생성 요청 DTO
     * @return 생성된 답변 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse createAnswer(String email, QnaAnswerCreateRequest request) {
        return answerService.createAnswer(email, request);
    }

    /**
     * 답변 수정
     *
     * @param email    사용자 이메일
     * @param answerId 수정할 답변 ID
     * @param request  답변 수정 요청 DTO
     * @return 수정된 답변 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse updateAnswer(String email, UUID answerId, QnaAnswerUpdateRequest request) {
        return answerService.updateAnswer(email, answerId, request);
    }

    /**
     * 답변 삭제
     *
     * @param email    사용자 이메일
     * @param answerId 삭제할 답변 ID
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public void deleteAnswer(String email, UUID answerId) {
        answerService.deleteAnswer(email, answerId);
    }

    /**
     * GPT로 AI 답변 생성
     * - 요청한 사용자(현재 로그인 사용자)의 API 키 사용
     *
     * @param user 요청한 사용자 (현재 로그인 사용자, null 가능)
     * @param questionId AI 답변을 생성할 질문 ID
     * @return 생성된 AI 답변 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse createAiAnswer(User user, UUID questionId) {
        return answerService.createAiAnswer(user, questionId);
    }

    /**
     * 답변 채택/해제
     *
     * @param email             요청 사용자 이메일
     * @param answerId          채택/해제할 답변 ID
     * @param anonymousPassword 익명 질문 비밀번호
     * @return 업데이트된 답변 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse acceptAnswer(String email, UUID answerId, String anonymousPassword) {
        return answerService.acceptAnswer(email, answerId, anonymousPassword);
    }

    /**
     * 답변 추천 (upvote)
     *
     * @param email    사용자 이메일
     * @param answerId 답변 ID
     * @return 업데이트된 답변 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse upvoteAnswer(String email, UUID answerId) {
        return answerService.upvoteAnswer(email, answerId);
    }

    /**
     * 답변 비추천 (downvote)
     *
     * @param email    사용자 이메일
     * @param answerId 답변 ID
     * @return 업데이트된 답변 정보를 담은 응답 DTO
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaAnswerResponse downvoteAnswer(String email, UUID answerId) {
        return answerService.downvoteAnswer(email, answerId);
    }

    // ========== 태그 관련 메서드 ==========

    /**
     * GPT를 사용하여 추천 태그 목록 생성
     * - 요청한 사용자(현재 로그인 사용자)의 API 키 사용
     *
     * @param user 요청한 사용자 (현재 로그인 사용자, null 가능)
     * @param request 태그 생성 요청
     * @return 추천 태그 목록
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional(readOnly = true)
    public QnaTagGenerateResponse generateRecommendedTags(User user, QnaTagGenerateRequest request) {
        return tagService.generateRecommendedTags(user, request);
    }

    /**
     * 선택한 태그 목록을 질문에 저장
     *
     * @param email             사용자 이메일
     * @param questionId        질문 ID
     * @param tagNames          저장할 태그 이름 목록
     * @param anonymousPassword 익명 게시글 비밀번호
     * @return 업데이트된 질문 정보
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional
    public QnaQuestionResponse saveTagsToQuestion(String email, UUID questionId, List<String> tagNames, String anonymousPassword) {
        return tagService.saveTagsToQuestion(email, questionId, tagNames, anonymousPassword);
    }

    // ========== 배틀 데이터 수집 ==========

    /**
     * 특정 게시글의 질문과 우선순위에 따라 선택된 답변을 수집하여 문자열로 반환
     * 배틀 생성 전에 QnA 데이터를 수집하는 데 사용됩니다.
     *
     * <p>답변 선택 우선순위:
     * <ol>
     *   <li>채택된(Accepted) 답변 최우선</li>
     *   <li>채택된 답변이 없을 경우, 양수(+) 점수를 가진 답변 중 추천 수가 가장 높은 답변</li>
     *   <li>둘 다 없을 경우 기존대로 GPT 답변 사용</li>
     * </ol>
     *
     * <p>전제 조건: 1번이라도 GPT 응답이 만들어진 QNA 게시글만 가능
     *
     * @param questionId 조회할 질문 ID
     * @return 질문과 선택된 답변을 포함한 문자열
     * @throws GptAnswerNotFoundException GPT 답변이 없는 경우
     * @apiNote 점검O
     * @since 2025-01-15
     */
    @Transactional(readOnly = true)
    public String collectQnaDataForBattle(UUID questionId) {
        log.info("QnA 데이터 수집 요청: questionId={}", questionId);

        QnaQuestion question = questionService.findQuestionById(questionId);
        List<QnaAnswer> allAnswers = answerRepository.findByQuestionOrderByCreatedAtAsc(question);

        List<QnaAnswer> gptAnswers = filterGptAnswers(allAnswers);
        validateGptAnswersExist(questionId, gptAnswers);

        List<QnaAnswer> selectedAnswers = answerSelector.selectAnswersForBattle(allAnswers);

        log.info("QnA 답변 선택 완료: questionId={}, selectedAnswerCount={}",
                questionId, selectedAnswers.size());

        String collectedData = battleDataBuilder.buildBattleDataString(question, selectedAnswers, gptAnswers);
        log.info("QnA 데이터 수집 완료: questionId={}, dataLength={}", questionId, collectedData.length());

        return collectedData;
    }

    // ========== 내부 헬퍼 메서드 ==========

    /**
     * GPT 답변 필터링
     */
    private List<QnaAnswer> filterGptAnswers(List<QnaAnswer> allAnswers) {
        return allAnswers.stream()
                .filter(QnaAnswer::isAiAnswer)
                .collect(Collectors.toList());
    }

    /**
     * GPT 답변 존재 여부 검증
     *
     * @throws GptAnswerNotFoundException GPT 답변이 없는 경우
     */
    private void validateGptAnswersExist(UUID questionId, List<QnaAnswer> gptAnswers) {
        if (gptAnswers.isEmpty()) {
            log.warn("GPT 답변이 없습니다: questionId={}", questionId);
            throw new GptAnswerNotFoundException(QnaMessage.GPT_ANSWER_NOT_FOUND.format(questionId.toString()));
        }
    }
}
