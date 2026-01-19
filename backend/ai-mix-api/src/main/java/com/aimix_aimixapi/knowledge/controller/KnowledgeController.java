package com.aimix_aimixapi.knowledge.controller;

import com.aimix_aimixapi.auth.service.UserDetailsImpl;
import com.aimix_aimixapi.knowledge.dto.*;
import com.aimix_aimixapi.knowledge.service.KnowledgeCardService;
import com.aimix_aimixapi.knowledge.service.KnowledgeCardLikeService;
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

import java.util.List;
import java.util.UUID;

/**
 * 지식백과 컨트롤러
 * - 개념 카드 CRUD API
 */
@Log4j2
@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeCardService cardService;
    private final KnowledgeCardLikeService cardLikeService;

    // ========== 개념 카드 API ==========

    /**
     * 지식 카드 존재 여부 확인
     * GET /api/v1/knowledge/cards/check?sourceId={uuid}&sourceType={QNA|CHAT|BATTLE}
     * 프론트에서 버튼 상태 결정을 위해 사용
     */
    @GetMapping("/cards/check")
    public ResponseEntity<KnowledgeCardExistsResponse> checkCardExists(
            @RequestParam UUID sourceId,
            @RequestParam String sourceType) {

        log.info("지식 카드 존재 여부 확인 요청: sourceId={}, sourceType={}", sourceId, sourceType);

        // sourceType 문자열을 enum으로 변환
        com.aimix_aimixapi.knowledge.entity.SourceType type;
        try {
            type = com.aimix_aimixapi.knowledge.entity.SourceType.valueOf(sourceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 sourceType: {}", sourceType);
            throw new IllegalArgumentException("잘못된 sourceType입니다. QNA, CHAT, BATTLE 중 하나여야 합니다.");
        }

        KnowledgeCardExistsResponse response = cardService.checkCardExists(sourceId, type);
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 미리보기 생성 (QnA에서) - 저장하지 않음
     * POST /api/v1/knowledge/cards/from-qna/preview
     */
    @PostMapping(value = "/cards/from-qna/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KnowledgeCardResponse> previewCardFromQna(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody KnowledgeCardFromQnaRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("QnA에서 카드 미리보기 생성 요청: email={}, questionId={}", email, request.getQuestionId());

        KnowledgeCardResponse response = cardService.previewCardFromQna(email, request.getQuestionId());
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 생성 (QnA에서) - preview 후 실제 저장
     * POST /api/v1/knowledge/cards/from-qna
     * Body에 sourceId와 sourceType이 없으면 questionId 쿼리 파라미터로 자동 설정
     * 
     * 사용 방법:
     * 1. Body에 sourceId와 sourceType 포함: POST /api/v1/knowledge/cards/from-qna
     * 2. Body에 없으면: POST /api/v1/knowledge/cards/from-qna?questionId={uuid}
     */
    @PostMapping(value = "/cards/from-qna", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KnowledgeCardResponse> createCardFromQna(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) UUID questionId,
            @Valid @RequestBody KnowledgeCardCreateRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("QnA에서 카드 생성 요청: email={}, questionId={}, title={}, sourceId={}, sourceType={}", 
                email, questionId, request.getTitle(), request.getSourceId(), request.getSourceType());

        // sourceId가 없으면 questionId로 자동 설정
        if (request.getSourceId() == null) {
            if (questionId == null) {
                log.error("sourceId와 questionId가 모두 null입니다. sourceId 또는 questionId를 제공해야 합니다.");
                throw new IllegalArgumentException("sourceId 또는 questionId는 필수입니다.");
            }
            request.setSourceId(questionId);
            log.info("sourceId가 null이어서 questionId로 설정: {}", questionId);
        }
        
        // sourceType이 없으면 QNA로 자동 설정
        if (request.getSourceType() == null) {
            request.setSourceType(com.aimix_aimixapi.knowledge.entity.SourceType.QNA);
            log.info("sourceType이 null이어서 QNA로 설정");
        }

        KnowledgeCardResponse response = cardService.createCardFromQna(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 미리보기 생성 (챗봇에서) - 저장하지 않음
     * POST /api/v1/knowledge/cards/from-chat/preview
     */
    @PostMapping(value = "/cards/from-chat/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KnowledgeCardResponse> previewCardFromChat(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody KnowledgeCardFromChatRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("챗봇에서 카드 미리보기 생성 요청: email={}, sessionId={}", email, request.getSessionId());

        KnowledgeCardResponse response = cardService.previewCardFromChat(email, request.getSessionId());
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 생성 (챗봇에서) - preview 후 실제 저장
     * POST /api/v1/knowledge/cards/from-chat
     */
    @PostMapping(value = "/cards/from-chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KnowledgeCardResponse> createCardFromChat(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody KnowledgeCardCreateRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("챗봇에서 카드 생성 요청: email={}, title={}, sourceId={}", 
                email, request.getTitle(), request.getSourceId());

        KnowledgeCardResponse response = cardService.createCardFromChat(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 생성 (수동)
     * POST /api/v1/knowledge/cards
     */
    @PostMapping(value = "/cards", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KnowledgeCardResponse> createCard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody KnowledgeCardCreateRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("카드 생성 요청: email={}, title={}", email, request.getTitle());

        KnowledgeCardResponse response = cardService.createCard(email, request);
        log.info("카드 생성 완료: cardId={}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 개수 조회
     * GET /api/v1/knowledge/cards/count
     */
    @GetMapping("/cards/count")
    public ResponseEntity<KnowledgeCardCountResponse> getCardCount() {
        log.info("카드 개수 조회 요청");

        KnowledgeCardCountResponse response = cardService.getCardCount();
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 생성한 카드 개수 조회
     * GET /api/v1/knowledge/cards/my-count
     * 비회원 접근 시 0 반환
     */
    @GetMapping("/cards/my-count")
    public ResponseEntity<Long> getMyCardCount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 비회원 접근 시 0 반환
        if (userDetails == null || userDetails.getUser() == null) {
            log.info("내 카드 개수 조회 요청: 비회원 접근");
            return ResponseEntity.ok(0L);
        }

        String email = userDetails.getUser().getEmail();
        log.info("내 카드 개수 조회 요청: email={}", email);

        Long count = cardService.getMyCardCount(email);
        return ResponseEntity.ok(count);
    }

    /**
     * 카드 목록 조회
     * GET /api/v1/knowledge/cards?page=0&size=20&sort=updatedAt,desc
     */
    @GetMapping("/cards")
    public ResponseEntity<Page<KnowledgeCardListResponse>> getCardList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<KnowledgeCardListResponse> response = cardService.getCardList(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 검색
     * GET /api/v1/knowledge/cards/search?keyword=rest
     */
    @GetMapping("/cards/search")
    public ResponseEntity<Page<KnowledgeCardListResponse>> searchCards(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        Page<KnowledgeCardListResponse> response = cardService.searchCards(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 상세 조회 (slug 기준)
     * GET /api/v1/knowledge/cards/{slug}
     * slug는 제목에서 자동 생성된 URL 친화적 식별자입니다.
     * 예: "REST API" → slug: "rest-api"
     * 로그인한 사용자의 경우 좋아요 상태(isLiked)도 반환합니다.
     */
    @GetMapping("/cards/{slug}")
    public ResponseEntity<KnowledgeCardDetailResponse> getCardBySlug(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String slug) {
        log.info("카드 상세 조회 요청: slug={}", slug);

        String email = (userDetails != null && userDetails.getUser() != null) 
                ? userDetails.getUser().getEmail() 
                : null;
        
        KnowledgeCardDetailResponse response = cardService.getCardBySlug(slug, email);
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 생성한 카드 목록 조회
     * GET /api/v1/knowledge/cards/my-cards?page=0&size=20
     * 비회원 접근 시 빈 목록 반환
     */
    @GetMapping("/cards/my-cards")
    public ResponseEntity<Page<KnowledgeCardListResponse>> getMyCards(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        // 비회원 접근 시 빈 목록 반환
        if (userDetails == null || userDetails.getUser() == null) {
            log.info("내 카드 목록 조회 요청: 비회원 접근");
            Sort sort = sortDir.equalsIgnoreCase("ASC")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            return ResponseEntity.ok(Page.empty(pageable));
        }

        String email = userDetails.getUser().getEmail();
        log.info("내 카드 목록 조회 요청: email={}", email);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<KnowledgeCardListResponse> response = cardService.getMyCards(email, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 인기 카드 목록 조회 (추천순)
     * GET /api/v1/knowledge/cards/popular/upvotes?page=0&size=20
     */
    @GetMapping("/cards/popular/upvotes")
    public ResponseEntity<Page<KnowledgeCardListResponse>> getPopularCardsByUpvotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeCardListResponse> response = cardService.getPopularCardsByUpvotes(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 인기 카드 목록 조회 (조회순)
     * GET /api/v1/knowledge/cards/popular/views?page=0&size=20
     */
    @GetMapping("/cards/popular/views")
    public ResponseEntity<Page<KnowledgeCardListResponse>> getPopularCardsByViews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<KnowledgeCardListResponse> response = cardService.getPopularCardsByViews(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 조회수 TOP10 카드 조회
     * GET /api/v1/knowledge/cards/top10/views
     * 사이드바에서 사용할 TOP10 조회수 카드 목록
     */
    @GetMapping("/cards/top10/views")
    public ResponseEntity<List<KnowledgeCardListResponse>> getTop10CardsByViews() {
        log.info("조회수 TOP10 카드 조회 요청");

        List<KnowledgeCardListResponse> response = cardService.getTop10CardsByViews();
        return ResponseEntity.ok(response);
    }

    /**
     * 좋아요 수 TOP10 카드 조회
     * GET /api/v1/knowledge/cards/top10/likes
     * 사이드바에서 사용할 TOP10 좋아요 카드 목록
     */
    @GetMapping("/cards/top10/likes")
    public ResponseEntity<List<KnowledgeCardListResponse>> getTop10CardsByLikes() {
        log.info("좋아요 수 TOP10 카드 조회 요청");

        List<KnowledgeCardListResponse> response = cardService.getTop10CardsByLikes();
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 수정
     * PUT /api/v1/knowledge/cards/{cardId}
     */
    @PutMapping(value = "/cards/{cardId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KnowledgeCardResponse> updateCard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long cardId,
            @Valid @RequestBody KnowledgeCardUpdateRequest request) {

        String email = userDetails.getUser().getEmail();
        log.info("카드 수정 요청: email={}, cardId={}", email, cardId);

        KnowledgeCardResponse response = cardService.updateCard(email, cardId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 카드 삭제
     * DELETE /api/v1/knowledge/cards/{cardId}
     * 기여자만 삭제 가능하며, 연관된 모든 데이터(좋아요 기록 등)를 함께 삭제합니다.
     */
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long cardId) {

        String email = userDetails.getUser().getEmail();
        log.info("카드 삭제 요청: email={}, cardId={}", email, cardId);

        cardService.deleteCard(email, cardId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 카드 추천 (기존 메서드 - 하위 호환성 유지)
     * POST /api/v1/knowledge/cards/{cardId}/upvote
     * @deprecated 사용자 인증이 없는 메서드입니다. 좋아요 기능을 사용하세요.
     */
    @Deprecated
    @PostMapping("/cards/{cardId}/upvote")
    public ResponseEntity<Void> upvoteCard(@PathVariable Long cardId) {
        log.info("카드 추천 요청: cardId={}", cardId);

        cardService.upvoteCard(cardId);
        return ResponseEntity.ok().build();
    }

    /**
     * 카드 좋아요 (토글 기능)
     * POST /api/v1/knowledge/cards/{cardId}/like
     * 로그인한 사용자만 좋아요 가능
     * 이미 좋아요한 경우 취소, 좋아요하지 않은 경우 추가
     */
    @PostMapping("/cards/{cardId}/like")
    public ResponseEntity<KnowledgeCardLikeResponse> likeCard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long cardId) {

        String email = userDetails.getUser().getEmail();
        log.info("카드 좋아요 요청: email={}, cardId={}", email, cardId);

        KnowledgeCardResponse cardResponse = cardLikeService.toggleLikeCard(email, cardId);
        
        // 사용자가 좋아요했는지 확인
        boolean isLiked = cardLikeService.isLikedByUser(email, cardId);

        KnowledgeCardLikeResponse response = KnowledgeCardLikeResponse.builder()
                .isLiked(isLiked)
                .upvoteCount(cardResponse.getUpvoteCount())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 관련 카드 조회
     * GET /api/v1/knowledge/cards/{cardId}/related
     */
    @GetMapping("/cards/{cardId}/related")
    public ResponseEntity<List<KnowledgeCardListResponse>> getRelatedCards(@PathVariable Long cardId) {
        log.info("관련 카드 조회 요청: cardId={}", cardId);

        List<KnowledgeCardListResponse> response = cardService.getRelatedCards(cardId);
        return ResponseEntity.ok(response);
    }

}