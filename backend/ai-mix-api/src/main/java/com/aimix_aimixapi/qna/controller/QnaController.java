package com.aimix_aimixapi.qna.controller;

import com.aimix_aimixapi.auth.service.UserDetailsImpl;
import com.aimix_aimixapi.common.exception.domain.AccessDeniedException;
import com.aimix_aimixapi.qna.dto.qna.*;
import com.aimix_aimixapi.qna.dto.tag.QnaTagGenerateRequest;
import com.aimix_aimixapi.qna.dto.tag.QnaTagGenerateResponse;
import com.aimix_aimixapi.qna.dto.tag.QnaTagSaveRequest;
import com.aimix_aimixapi.qna.service.qna.QnaService;
import com.aimix_aimixapi.user.dto.UserProfileResponse;
import com.aimix_aimixapi.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * QnA 컨트롤러
 * - 질문/답변 CRUD API
 * - 태그 관리 API
 * - 검색 API
 * - 추천 API
 * - JWT 토큰 인증 필요
 */
@Log4j2
@RestController
@RequestMapping("/api/v1/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final UserService userService;

    // ========== 질문 관련 API ==========

    /**
     * 질문 생성
     * POST /api/v1/qna/questions
     * 익명 게시글인 경우 인증 불필요
     */
    @PostMapping(value = "/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QnaQuestionResponse> createQuestion(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody QnaQuestionCreateRequest request) {

        String email = null;
        if (userDetails != null) {
            email = userDetails.getUser().getEmail();
        }

        // 익명 게시글이 아닌 경우 로그인 필요
        if (!request.getIsAnonymous() && email == null) {
            throw new AccessDeniedException("일반 게시글 작성을 위해서는 로그인이 필요합니다");
        }

        log.info("질문 생성 요청: email={}, title={}, isAnonymous={}", email, request.getTitle(), request.getIsAnonymous());

        QnaQuestionResponse response = qnaService.createQuestion(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 질문 목록 조회
     * GET /api/v1/qna/questions?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping("/questions")
    public ResponseEntity<Page<QnaQuestionListResponse>> getQuestionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<QnaQuestionListResponse> response = qnaService.getQuestionList(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 질문 검색
     * GET /api/v1/qna/questions/search?keyword=검색어&searchType=title&page=0&size=10
     * searchType: title(제목만), author(작성자만), body(내용만), tag(태그만), all(전체, 기본값)
     */
    @GetMapping("/questions/search")
    public ResponseEntity<Page<QnaQuestionListResponse>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "all") String searchType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<QnaQuestionListResponse> response = qnaService.searchQuestions(keyword, searchType, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 질문 상세 조회
     * GET /api/v1/qna/questions/{questionId}
     */
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<QnaQuestionDetailResponse> getQuestionDetail(
            @PathVariable UUID questionId) {

        QnaQuestionDetailResponse response = qnaService.getQuestionDetail(questionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 질문 수정
     * PUT /api/v1/qna/questions/{questionId}
     */
    @PutMapping(value = "/questions/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QnaQuestionResponse> updateQuestion(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID questionId,
            @Valid @RequestBody QnaQuestionUpdateRequest request) {

        String email = null;
        if (userDetails != null) {
            email = userDetails.getUser().getEmail();
        }
        log.info("질문 수정 요청: email={}, questionId={}", email, questionId);

        QnaQuestionResponse response = qnaService.updateQuestion(email, questionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 질문 삭제
     * DELETE /api/v1/qna/questions/{questionId}
     * 익명 게시글인 경우 RequestBody에 anonymousPassword 필요
     */
    @DeleteMapping(value = "/questions/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteQuestion(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID questionId,
            @RequestBody(required = false) QnaQuestionDeleteRequest request) {

        String email = null;
        if (userDetails != null) {
            email = userDetails.getUser().getEmail();
        }

        String anonymousPassword = request != null ? request.getAnonymousPassword() : null;
        log.info("질문 삭제 요청: email={}, questionId={}", email, questionId);

        qnaService.deleteQuestion(email, questionId, anonymousPassword);
        return ResponseEntity.noContent().build();
    }

    // ========== 답변 관련 API ==========

    /**
     * 답변 생성
     * POST /api/v1/qna/answers
     * 로그인한 사용자만 답변 작성 가능
     * SecurityConfig에서 인증을 요구하므로 userDetails는 null이 될 수 없음
     */
    @PostMapping(value = "/answers", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QnaAnswerResponse> createAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody QnaAnswerCreateRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("답변 생성 요청: email={}, questionId={}", email, request.getQuestionId());

        QnaAnswerResponse response = qnaService.createAnswer(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GPT로 AI 답변 생성
     * POST /api/v1/qna/questions/{questionId}/ai-answer
     * - 요청한 사용자(현재 로그인 사용자)의 API 키 사용
     */
    @PostMapping("/questions/{questionId}/ai-answer")
    public ResponseEntity<QnaAnswerResponse> createAiAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID questionId) {

        log.info("GPT 답변 생성 요청: questionId={}, userId={}", 
                questionId, userDetails != null ? userDetails.getUser().getId() : null);

        QnaAnswerResponse response = qnaService.createAiAnswer(
                userDetails != null ? userDetails.getUser() : null, questionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 답변 수정
     * PUT /api/v1/qna/answers/{answerId}
     * 로그인한 사용자만 자신의 답변 수정 가능
     * SecurityConfig에서 인증을 요구하므로 userDetails는 null이 될 수 없음
     */
    @PutMapping(value = "/answers/{answerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QnaAnswerResponse> updateAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID answerId,
            @Valid @RequestBody QnaAnswerUpdateRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("답변 수정 요청: email={}, answerId={}", email, answerId);

        QnaAnswerResponse response = qnaService.updateAnswer(email, answerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 답변 삭제
     * DELETE /api/v1/qna/answers/{answerId}
     * 로그인한 사용자만 자신의 답변 삭제 가능
     * SecurityConfig에서 인증을 요구하므로 userDetails는 null이 될 수 없음
     */
    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID answerId) {

        String email = userDetails.getUser().getEmail();
        log.info("답변 삭제 요청: email={}, answerId={}", email, answerId);

        qnaService.deleteAnswer(email, answerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 답변 추천 (upvote) - 토글 기능
     * POST /api/v1/qna/answers/{answerId}/upvote
     * 로그인한 사용자만 추천 가능
     * Stack Overflow 스타일: 같은 타입을 다시 누르면 취소, 다른 타입을 누르면 전환
     */
    @PostMapping("/answers/{answerId}/upvote")
    public ResponseEntity<QnaAnswerResponse> upvoteAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID answerId) {

        String email = userDetails.getUser().getEmail();
        log.info("답변 추천 요청: email={}, answerId={}", email, answerId);

        QnaAnswerResponse response = qnaService.upvoteAnswer(email, answerId);
        return ResponseEntity.ok(response);
    }

    /**
     * 답변 비추천 (downvote) - 토글 기능
     * POST /api/v1/qna/answers/{answerId}/downvote
     * 로그인한 사용자만 비추천 가능
     * Stack Overflow 스타일: 같은 타입을 다시 누르면 취소, 다른 타입을 누르면 전환
     */
    @PostMapping("/answers/{answerId}/downvote")
    public ResponseEntity<QnaAnswerResponse> downvoteAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID answerId) {

        String email = userDetails.getUser().getEmail();
        log.info("답변 비추천 요청: email={}, answerId={}", email, answerId);

        QnaAnswerResponse response = qnaService.downvoteAnswer(email, answerId);
        return ResponseEntity.ok(response);
    }

    /**
     * 답변 채택/해제
     * POST /api/v1/qna/answers/{answerId}/accept
     * <p>
     * - 질문 작성자만 수행 가능
     * - 익명 질문의 경우 비밀번호로 권한 검증
     * - 이미 채택된 답변을 다시 호출하면 채택 해제 (토글)
     * @since 2025-12-17
     */
    @PostMapping("/answers/{answerId}/accept")
    public ResponseEntity<QnaAnswerResponse> acceptAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID answerId,
            @RequestBody(required = false) QnaAnswerAcceptRequest request) {

        String email = null;
        if (userDetails != null) {
            email = userDetails.getUser().getEmail();
        }
        String anonymousPassword = request != null ? request.getAnonymousPassword() : null;

        log.info("답변 채택/해제 요청: email={}, answerId={}", email, answerId);

        QnaAnswerResponse response = qnaService.acceptAnswer(email, answerId, anonymousPassword);
        return ResponseEntity.ok(response);
    }

    // ========== 태그 관련 API ==========

    /**
     * 태그 자동 생성 (미리보기) - 저장하지 않음
     * POST /api/v1/qna/tags/preview
     * questionId를 기반으로 질문을 조회하여 GPT를 사용하여 추천 태그 목록을 생성
     * DB에 저장하지 않고 태그 리스트만 반환 (사용자가 선택하여 추가)
     * - 요청한 사용자(현재 로그인 사용자)의 API 키 사용
     */
    @PostMapping(value = "/tags/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QnaTagGenerateResponse> previewTags(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody QnaTagGenerateRequest request) {

        log.info("태그 미리보기 요청: questionId={}, userId={}", 
                request.getQuestionId(), userDetails != null ? userDetails.getUser().getId() : null);

        QnaTagGenerateResponse response = qnaService.generateRecommendedTags(
                userDetails != null ? userDetails.getUser() : null, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 태그 저장
     * PATCH /api/v1/qna/questions/{questionId}/tags
     * 선택한 태그 목록을 질문에 연결하여 저장
     * 기존 태그는 모두 제거하고 새로운 태그로 교체
     * 익명 게시글인 경우 RequestBody에 anonymousPassword 필요
     */
    @PatchMapping(value = "/questions/{questionId}/tags", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QnaQuestionResponse> saveTags(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID questionId,
            @Valid @RequestBody QnaTagSaveRequest request) {

        String email = null;
        if (userDetails != null) {
            email = userDetails.getUser().getEmail();
        }

        log.info("태그 저장 요청: email={}, questionId={}, tags={}", email, questionId, request.getTags());

        QnaQuestionResponse response = qnaService.saveTagsToQuestion(email, questionId, request.getTags(), request.getAnonymousPassword());
        return ResponseEntity.ok(response);
    }

    // ========== 사용자 프로필 관련 API ==========

    /**
     * QnA 게시판에서 사용자 프로필 정보 조회
     * GET /api/v1/qna/users/{nickname}
     * - 작성자를 클릭했을 때 표시되는 간단한 정보
     * - 공개 정보만 포함 (이메일, 생년월일 등 민감 정보 제외)
     * - QnA 활동 통계 정보 포함
     * - 인증 불필요 (공개 정보)
     *
     * @param nickname 사용자 닉네임
     * @return 사용자 프로필 정보 (QnA 통계 포함)
     * @apiNote 점검O
     * @since 2025-12-30
     */
    @GetMapping("/users/{nickname}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable String nickname) {

        log.info("사용자 프로필 조회 요청: nickname={}", nickname);

        UserProfileResponse response = userService.getUserProfileByNickname(nickname);
        return ResponseEntity.ok(response);
    }

}
