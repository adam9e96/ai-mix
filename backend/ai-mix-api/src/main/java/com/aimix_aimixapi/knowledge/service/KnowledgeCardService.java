package com.aimix_aimixapi.knowledge.service;

import com.aimix_aimixapi.chat.entity.ChatMessage;
import com.aimix_aimixapi.chat.service.ChatMessageService;
import com.aimix_aimixapi.chat.service.ChatSessionService;
import com.aimix_aimixapi.common.exception.domain.knowledge.InvalidSourceTypeException;
import com.aimix_aimixapi.common.exception.domain.knowledge.card.DuplicateKnowledgeCardException;
import com.aimix_aimixapi.common.exception.domain.knowledge.card.EmptyConversationException;
import com.aimix_aimixapi.common.exception.domain.knowledge.card.KnowledgeCardAccessDeniedException;
import com.aimix_aimixapi.common.exception.domain.knowledge.card.KnowledgeCardNotFoundException;
import com.aimix_aimixapi.common.exception.domain.knowledge.gpt.CardGenerationFailedException;
import com.aimix_aimixapi.common.exception.domain.knowledge.gpt.CardParsingFailedException;
import com.aimix_aimixapi.knowledge.message.KnowledgeMessage;
import com.aimix_aimixapi.gpt.util.GptResponseUtils;
import com.aimix_aimixapi.knowledge.dto.*;
import com.aimix_aimixapi.knowledge.entity.ContributionType;
import com.aimix_aimixapi.knowledge.entity.KnowledgeCard;
import com.aimix_aimixapi.knowledge.entity.SourceType;
import com.aimix_aimixapi.knowledge.repository.KnowledgeCardRepository;
import com.aimix_aimixapi.qna.service.qna.QnaService;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 지식백과 카드 서비스
 * 개념 카드의 CRUD 및 조회 기능을 제공합니다.
 * - 카드 생성, 수정, 조회
 * - 카드 목록 조회 및 검색
 * - 인기 카드 조회
 * - 관련 카드 조회
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class KnowledgeCardService {

    private final KnowledgeCardRepository cardRepository;
    private final CardContributionService contributionService;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final KnowledgeCardLikeService cardLikeService;
    private final KnowledgeCardGptService gptService;
    private final KnowledgeCardSlugService slugService;
    private final KnowledgeCardConverter converter;
    @Lazy
    private final QnaService qnaService;

    /**
     * 개념 카드 생성
     * 사용자가 입력한 정보로 새로운 개념 카드를 생성합니다.
     *
     * @param email   사용자 이메일 (null 불가)
     * @param request 카드 생성 요청 DTO (null 불가)
     * @return 생성된 카드 응답 DTO
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public KnowledgeCardResponse createCard(String email, KnowledgeCardCreateRequest request) {
        log.info("개념 카드 생성 요청: email={}, title={}", email, request.getTitle());

        User user = userService.findUserByEmail(email);

        // 슬러그 생성 (제목 기반)
        String slug = slugService.generateSlug(request.getTitle());

        KnowledgeCard card = KnowledgeCard.builder()
                .title(request.getTitle())
                .slug(slug)
                .oneLineDefinition(request.getOneLineDefinition())
                .corePoints(request.getCorePoints() != null ? request.getCorePoints() : new ArrayList<>())
                .commonMistakes(request.getCommonMistakes() != null ? request.getCommonMistakes() : new ArrayList<>())
                .relatedConcepts(request.getRelatedConcepts() != null ? request.getRelatedConcepts() : new ArrayList<>())
                .sourceType(request.getSourceType())
                .sourceId(request.getSourceId())
                .contributor(user)
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .build();

        // 카드 저장
        KnowledgeCard savedCard = cardRepository.save(card);

        // 기여 이력 기록
        contributionService.recordContribution(savedCard, user, ContributionType.CREATE, "카드 생성");

        log.info("개념 카드 생성 완료: cardId={}, title={}", savedCard.getId(), savedCard.getTitle());

        return converter.convertToCardResponse(savedCard);
    }

    /**
     * QnA에서 개념 카드 미리보기 생성 (저장하지 않음)
     * GPT를 사용하여 QnA 질문과 답변을 분석하고 카드 데이터를 생성하여 반환합니다.
     *
     * @param email      사용자 이메일 (null 불가)
     * @param questionId QnA 질문 ID (null 불가)
     * @return 미리보기용 카드 응답 DTO
     * @throws CardGenerationFailedException GPT API 호출 실패 시
     * @throws CardParsingFailedException    GPT 응답 파싱 실패 시
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional(readOnly = true)
    public KnowledgeCardResponse previewCardFromQna(String email, UUID questionId) {
        return previewCardFromSource(email, questionId, SourceType.QNA);
    }

    /**
     * QnA에서 개념 카드 생성 (preview 후 실제 저장)
     * preview에서 생성된 내용을 사용자가 수정한 후 실제 카드로 저장합니다.
     *
     * @param email   사용자 이메일 (null 불가)
     * @param request 카드 생성 요청 DTO (null 불가)
     * @return 생성된 카드 응답 DTO
     * @throws DuplicateKnowledgeCardException 이미 생성된 카드가 존재하는 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public KnowledgeCardResponse createCardFromQna(String email, KnowledgeCardCreateRequest request) {
        return createCardFromSource(email, request, SourceType.QNA);
    }

    /**
     * 챗봇에서 개념 카드 생성 (preview 후 실제 저장)
     * preview에서 생성된 내용을 사용자가 수정한 후 실제 카드로 저장합니다.
     *
     * @param email   사용자 이메일 (null 불가)
     * @param request 카드 생성 요청 DTO (null 불가)
     * @return 생성된 카드 응답 DTO
     * @throws DuplicateKnowledgeCardException 이미 생성된 카드가 존재하는 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public KnowledgeCardResponse createCardFromChat(String email, KnowledgeCardCreateRequest request) {
        return createCardFromSource(email, request, SourceType.CHAT);
    }

    /**
     * 챗봇에서 개념 카드 미리보기 생성 (저장하지 않음)
     * GPT를 사용하여 대화 내용을 분석하고 카드 데이터를 생성하여 반환합니다.
     *
     * @param email     사용자 이메일 (null 불가)
     * @param sessionId 채팅 세션 ID (null 불가)
     * @return 미리보기용 카드 응답 DTO
     * @throws EmptyConversationException    대화 내용이 없는 경우
     * @throws CardGenerationFailedException GPT API 호출 실패 시
     * @throws CardParsingFailedException    GPT 응답 파싱 실패 시
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional(readOnly = true)
    public KnowledgeCardResponse previewCardFromChat(String email, UUID sessionId) {
        return previewCardFromSource(email, sessionId, SourceType.CHAT);
    }

    /**
     * 출처별 개념 카드 미리보기 생성 (공통 로직)
     * QnA와 Chat 모두에서 사용하는 공통 미리보기 생성 로직입니다.
     *
     * @param email      사용자 이메일 (null 불가)
     * @param sourceId   출처 ID (질문 ID 또는 세션 ID, null 불가)
     * @param sourceType 출처 타입 (QNA 또는 CHAT, null 불가)
     * @return 미리보기용 카드 응답 DTO
     * @throws EmptyConversationException    대화 내용이 없는 경우 (CHAT만)
     * @throws CardGenerationFailedException GPT API 호출 실패 시
     * @throws CardParsingFailedException    GPT 응답 파싱 실패 시
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional(readOnly = true)
    public KnowledgeCardResponse previewCardFromSource(String email, UUID sourceId, SourceType sourceType) {
        log.info("출처별 개념 카드 미리보기 생성 요청: email={}, sourceId={}, sourceType={}", email, sourceId, sourceType);

        // 1. 사용자 조회
        User user = userService.findUserByEmail(email);

        // 2. 출처별 권한 확인 및 텍스트 수집
        String textContent = collectTextFromSource(sourceId, sourceType, user);

        // 3. GPT API 호출하여 카드 데이터 생성
        KnowledgeCardCreateRequest cardRequest = gptService.generateCardFromText(
                textContent, sourceId, sourceType, user);

        // 4. 미리보기용 응답 생성 (저장하지 않음)
        return converter.convertToPreviewResponse(cardRequest, user.getId(), user.getNickname());
    }

    /**
     * 출처별 개념 카드 생성 (공통 로직)
     * QnA와 Chat 모두에서 사용하는 공통 카드 생성 로직입니다.
     *
     * @param email      사용자 이메일 (null 불가)
     * @param request    카드 생성 요청 DTO (null 불가)
     * @param sourceType 출처 타입 (QNA 또는 CHAT, null 불가)
     * @return 생성된 카드 응답 DTO
     * @throws DuplicateKnowledgeCardException 이미 생성된 카드가 존재하는 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public KnowledgeCardResponse createCardFromSource(
            String email, KnowledgeCardCreateRequest request, SourceType sourceType) {
        log.info("출처별 개념 카드 생성 요청: email={}, title={}, sourceId={}, sourceType={}",
                email, request.getTitle(), request.getSourceId(), sourceType);

        // 1. 중복 체크
        checkDuplicateCard(request);

        // 2. 출처별 권한 확인
        if (request.getSourceId() != null) {
            User user = userService.findUserByEmail(email);
            checkSourceAccess(request.getSourceId(), sourceType, user);
        }

        // 3. 카드 생성
        return createCard(email, request);
    }

    /**
     * 중복 카드 체크
     * sourceId와 sourceType으로 이미 생성된 카드가 있는지 확인합니다.
     *
     * @param request 카드 생성 요청 DTO (null 불가)
     * @throws DuplicateKnowledgeCardException 이미 생성된 카드가 존재하는 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    private void checkDuplicateCard(KnowledgeCardCreateRequest request) {
        if (request.getSourceId() == null || request.getSourceType() == null) {
            return;
        }

        Optional<KnowledgeCard> existingCard = cardRepository.findBySourceTypeAndSourceId(
                request.getSourceType(), request.getSourceId());

        if (existingCard.isPresent()) {
            log.warn("이미 생성된 지식 카드가 존재합니다: sourceType={}, sourceId={}, cardId={}",
                    request.getSourceType(), request.getSourceId(), existingCard.get().getId());
            String message = String.format("해당 UUID(%s)로 이미 지식 카드가 생성되었습니다. 카드 ID: %d",
                    request.getSourceId(), existingCard.get().getId());
            throw new DuplicateKnowledgeCardException(KnowledgeMessage.DUPLICATE_CARD.format(message));
        }
    }

    /**
     * 출처별 텍스트 수집
     * sourceType에 따라 QnA 데이터 또는 채팅 대화 내용을 텍스트로 수집합니다.
     *
     * @param sourceId   출처 ID (질문 ID 또는 세션 ID, null 불가)
     * @param sourceType 출처 타입 (QNA 또는 CHAT, null 불가)
     * @param user       사용자 (null 불가)
     * @return 수집된 텍스트 내용
     * @throws EmptyConversationException 대화 내용이 없는 경우 (CHAT만)
     * @apiNote 점검O
     * @since 2025-12-29
     */
    private String collectTextFromSource(UUID sourceId, SourceType sourceType, User user) {
        return switch (sourceType) {
            case QNA -> {
                String qnaText = qnaService.collectQnaDataForBattle(sourceId);
                log.info("QnA 데이터 텍스트 길이: {}", qnaText.length());
                yield qnaText;
            }
            case CHAT -> {
                // 세션 조회 및 권한 확인
                chatSessionService.findByIdWithAuthCheck(sourceId, user);

                // 대화 메시지 조회
                List<ChatMessage> messages = chatMessageService.findBySessionIdOrderByCreatedAtAsc(sourceId);
                if (messages.isEmpty()) {
                    throw new EmptyConversationException(KnowledgeMessage.EMPTY_CONVERSATION.getMessage());
                }

                log.info("조회된 메시지 개수: {}", messages.size());

                // 대화 내용을 텍스트로 변환
                String conversationText = GptResponseUtils.buildConversationText(messages);
                log.info("대화 내용 텍스트 길이: {}", conversationText.length());
                yield conversationText;
            }
            default -> throw new InvalidSourceTypeException(
                    KnowledgeMessage.INVALID_SOURCE_TYPE.format(sourceType != null ? sourceType.name() : "null"));
        };
    }

    /**
     * 출처별 접근 권한 확인
     * sourceType에 따라 필요한 권한 확인을 수행합니다.
     *
     * @param sourceId   출처 ID (질문 ID 또는 세션 ID, null 불가)
     * @param sourceType 출처 타입 (QNA 또는 CHAT, null 불가)
     * @param user       사용자 (null 불가)
     * @apiNote 점검O
     * @since 2025-12-29
     */
    private void checkSourceAccess(UUID sourceId, SourceType sourceType, User user) {
        switch (sourceType) {
            case QNA -> {
                // QnA 질문은 공개이므로 별도 권한 확인 불필요
                // 필요시 QnaService에 권한 확인 메서드 추가 가능
            }
            case CHAT -> {
                // 세션 권한 확인
                chatSessionService.findByIdWithAuthCheck(sourceId, user);
            }
            default -> {
                // 다른 타입은 권한 확인 불필요
            }
        };
    }

    /**
     * 개념 카드 수정
     * 기여자만 수정 가능하며, 수정 이력이 기록됩니다.
     *
     * @param email   사용자 이메일 (null 불가)
     * @param cardId  카드 ID (null 불가)
     * @param request 카드 수정 요청 DTO (null 불가)
     * @return 수정된 카드 응답 DTO
     * @throws KnowledgeCardNotFoundException     카드를 찾을 수 없는 경우
     * @throws KnowledgeCardAccessDeniedException 수정 권한이 없는 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public KnowledgeCardResponse updateCard(String email, Long cardId, KnowledgeCardUpdateRequest request) {
        log.info("개념 카드 수정 요청: email={}, cardId={}", email, cardId);

        User user = userService.findUserByEmail(email);
        KnowledgeCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new KnowledgeCardNotFoundException(KnowledgeMessage.CARD_NOT_FOUND.format(String.valueOf(cardId))));

        // 권한 확인 (기여자만 수정 가능)
        if (card.getContributor() == null || !card.getContributor().getId().equals(user.getId())) {
            throw new KnowledgeCardAccessDeniedException(KnowledgeMessage.CARD_ACCESS_DENIED.getMessage());
        }

        // 제목 수정 (제목이 변경되면 슬러그도 재생성)
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            String newTitle = request.getTitle().trim();
            String oldTitle = card.getTitle();
            if (!newTitle.equals(oldTitle)) {
                card.setTitle(newTitle);
                // 슬러그 재생성 (제목 변경 시)
                String newSlug = slugService.generateSlug(newTitle);
                card.setSlug(newSlug);
                log.debug("카드 제목 및 슬러그 변경: cardId={}, oldTitle={}, newTitle={}, newSlug={}",
                        cardId, oldTitle, newTitle, newSlug);
            }
        }

        // 한 줄 정의 수정
        card.setOneLineDefinition(request.getOneLineDefinition());

        // 핵심 포인트 수정
        if (request.getCorePoints() != null) {
            card.setCorePoints(request.getCorePoints());
        }

        // 자주 틀리는 오해 수정
        if (request.getCommonMistakes() != null) {
            card.setCommonMistakes(request.getCommonMistakes());
        }

        // 관련 개념 수정
        if (request.getRelatedConcepts() != null) {
            card.setRelatedConcepts(request.getRelatedConcepts());
        }

        // 공개 여부 수정
        if (request.getIsPublished() != null) {
            card.setIsPublished(request.getIsPublished());
        }

        KnowledgeCard savedCard = cardRepository.save(card);

        // 기여 이력 기록
        contributionService.recordContribution(savedCard, user, ContributionType.UPDATE, "카드 수정");

        log.info("개념 카드 수정 완료: cardId={}", savedCard.getId());

        return converter.convertToCardResponse(savedCard);
    }

    /**
     * 개념 카드 삭제
     * 기여자만 삭제 가능하며, 연관된 모든 데이터를 함께 삭제합니다.
     * - 좋아요 기록
     * - 기여 이력 (Cascade로 자동 삭제)
     *
     * @param email  사용자 이메일 (null 불가)
     * @param cardId 카드 ID (null 불가)
     * @throws KnowledgeCardNotFoundException     카드를 찾을 수 없는 경우
     * @throws KnowledgeCardAccessDeniedException 삭제 권한이 없는 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public void deleteCard(String email, Long cardId) {
        log.info("개념 카드 삭제 요청: email={}, cardId={}", email, cardId);

        User user = userService.findUserByEmail(email);
        KnowledgeCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new KnowledgeCardNotFoundException(KnowledgeMessage.CARD_NOT_FOUND.format(String.valueOf(cardId))));

        // 권한 확인 (기여자만 삭제 가능)
        if (card.getContributor() == null || !card.getContributor().getId().equals(user.getId())) {
            throw new KnowledgeCardAccessDeniedException(KnowledgeMessage.CARD_ACCESS_DENIED.getMessage());
        }

        // 3. 좋아요 기록 삭제
        cardLikeService.deleteAllLikesByCardId(cardId);
        log.debug("좋아요 기록 삭제 완료: cardId={}", cardId);

        // 4. 카드 삭제 (기여 이력은 Cascade로 자동 삭제)
        cardRepository.delete(card);
        log.info("개념 카드 삭제 완료: cardId={}, title={}", cardId, card.getTitle());
    }

    /**
     * 개념 카드 상세 조회 (slug)
     * 조회 시 조회수가 증가하며, 관련 카드와 좋아요 상태를 포함합니다.
     *
     * @param slug  카드 slug (null 불가)
     * @param email 사용자 이메일 (선택적, null 가능, 좋아요 상태 반환용)
     * @return 카드 상세 응답 DTO
     * @throws KnowledgeCardNotFoundException 카드를 찾을 수 없는 경우
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional
    public KnowledgeCardDetailResponse getCardBySlug(String slug, String email) {
        log.info("개념 카드 상세 조회: slug={}, email={}", slug, email);

        KnowledgeCard card = cardRepository.findBySlug(slug)
                .orElseThrow(() -> new KnowledgeCardNotFoundException(KnowledgeMessage.CARD_NOT_FOUND.format(slug)));

        return buildCardDetailResponse(card, email);
    }

    /**
     * 카드 상세 응답 생성 (공통 로직)
     *
     * @param card  카드 엔티티
     * @param email 사용자 이메일 (선택적, 로그인한 사용자의 좋아요 상태 반환)
     */
    private KnowledgeCardDetailResponse buildCardDetailResponse(KnowledgeCard card, String email) {
        // 조회수 증가 (트랜잭션 내에서 쓰기 작업 수행)
        card.incrementViewCount();
        cardRepository.save(card);

        // 관련 카드 조회
        List<KnowledgeCardListResponse> relatedCards = new ArrayList<>();
        if (card.getRelatedConcepts() != null && !card.getRelatedConcepts().isEmpty()) {
            relatedCards = card.getRelatedConcepts().stream()
                    .map(cardRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(converter::convertToCardListResponse)
                    .collect(Collectors.toList());
        }

        // 로그인한 사용자의 좋아요 상태 확인
        Boolean isLiked = null;
        if (email != null) {
            try {
                isLiked = cardLikeService.isLikedByUser(email, card.getId());
            } catch (Exception e) {
                log.warn("좋아요 상태 확인 실패: email={}, cardId={}", email, card.getId(), e);
                isLiked = false;
            }
        }

        return KnowledgeCardDetailResponse.builder()
                .card(converter.convertToCardResponse(card, isLiked))
                .relatedCards(relatedCards)
                .build();
    }

    /**
     * 카드 개수 조회
     * 전체, 공개, 비공개 카드 개수를 반환합니다.
     *
     * @return 카드 개수 응답 DTO
     * @apiNote 점검O
     * @since 2025-12-29
     */
    @Transactional(readOnly = true)
    public KnowledgeCardCountResponse getCardCount() {
        log.info("카드 개수 조회");

        long totalCount = cardRepository.count();
        long publishedCount = cardRepository.countByIsPublishedTrue();
        long unpublishedCount = totalCount - publishedCount;

        return KnowledgeCardCountResponse.builder()
                .totalCount(totalCount)
                .publishedCount(publishedCount)
                .unpublishedCount(unpublishedCount)
                .build();
    }

    /**
     * 내가 생성한 카드 개수 조회
     */
    @Transactional(readOnly = true)
    public Long getMyCardCount(String email) {
        log.info("내 카드 개수 조회: email={}", email);

        User user = userService.findUserByEmail(email);
        return cardRepository.countByContributor(user);
    }

    /**
     * 개념 카드 목록 조회
     * 공개된 카드만 조회합니다 (비회원도 접근 가능)
     */
    @Transactional(readOnly = true)
    public Page<KnowledgeCardListResponse> getCardList(Pageable pageable) {
        // 공개된 카드만 조회
        Page<KnowledgeCard> cards = cardRepository.findByIsPublishedTrueOrderByUpdatedAtDesc(pageable);
        return cards.map(converter::convertToCardListResponse);
    }

    /**
     * 개념 카드 검색
     * 공개된 카드에서만 검색합니다 (비회원도 접근 가능)
     */
    @Transactional(readOnly = true)
    public Page<KnowledgeCardListResponse> searchCards(String keyword, Pageable pageable) {
        // 공개된 카드만 검색
        Page<KnowledgeCard> cards = cardRepository.searchByKeyword(keyword, pageable);
        return cards.map(converter::convertToCardListResponse);
    }

    /**
     * 내가 생성한 카드 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<KnowledgeCardListResponse> getMyCards(String email, Pageable pageable) {
        log.info("내 카드 목록 조회: email={}", email);

        User user = userService.findUserByEmail(email);
        Page<KnowledgeCard> cards = cardRepository.findByContributorOrderByUpdatedAtDesc(user, pageable);
        return cards.map(converter::convertToCardListResponse);
    }

    /**
     * 인기 카드 목록 조회 (추천순)
     * 공개된 카드만 조회합니다 (비회원도 접근 가능)
     */
    @Transactional(readOnly = true)
    public Page<KnowledgeCardListResponse> getPopularCardsByUpvotes(Pageable pageable) {
        log.info("인기 카드 목록 조회 (추천순)");

        Page<KnowledgeCard> cards = cardRepository.findByIsPublishedTrueOrderByUpvoteCountDesc(pageable);
        return cards.map(converter::convertToCardListResponse);
    }

    /**
     * 인기 카드 목록 조회 (조회순)
     * 공개된 카드만 조회합니다 (비회원도 접근 가능)
     */
    @Transactional(readOnly = true)
    public Page<KnowledgeCardListResponse> getPopularCardsByViews(Pageable pageable) {
        log.info("인기 카드 목록 조회 (조회순)");

        Page<KnowledgeCard> cards = cardRepository.findByIsPublishedTrueOrderByViewCountDesc(pageable);
        return cards.map(converter::convertToCardListResponse);
    }

    /**
     * 조회수 TOP10 카드 조회
     * 공개된 카드만 조회합니다 (비회원도 접근 가능)
     */
    @Transactional(readOnly = true)
    public List<KnowledgeCardListResponse> getTop10CardsByViews() {
        log.info("조회수 TOP10 카드 조회");

        List<KnowledgeCard> cards = cardRepository.findTop10ByIsPublishedTrueOrderByViewCountDesc();
        return converter.convertToCardListResponseList(cards);
    }

    /**
     * 좋아요 수 TOP10 카드 조회
     * 공개된 카드만 조회합니다 (비회원도 접근 가능)
     */
    @Transactional(readOnly = true)
    public List<KnowledgeCardListResponse> getTop10CardsByLikes() {
        log.info("좋아요 수 TOP10 카드 조회");

        List<KnowledgeCard> cards = cardRepository.findTop10ByIsPublishedTrueOrderByUpvoteCountDesc();
        return converter.convertToCardListResponseList(cards);
    }

    /**
     * 개념 카드 추천 (기존 메서드 - 하위 호환성 유지)
     *
     * @deprecated 사용자 인증이 없는 메서드입니다. 좋아요 기능을 사용하세요.
     */
    @Deprecated
    @Transactional
    public void upvoteCard(Long cardId) {
        log.info("개념 카드 추천: cardId={}", cardId);

        KnowledgeCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new KnowledgeCardNotFoundException(KnowledgeMessage.CARD_NOT_FOUND.format(String.valueOf(cardId))));

        card.incrementUpvoteCount();
        cardRepository.save(card);
    }

    /**
     * 관련 카드 조회
     */
    @Transactional(readOnly = true)
    public List<KnowledgeCardListResponse> getRelatedCards(Long cardId) {
        KnowledgeCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new KnowledgeCardNotFoundException(KnowledgeMessage.CARD_NOT_FOUND.format(String.valueOf(cardId))));

        List<KnowledgeCard> relatedCards = new ArrayList<>();

        return converter.convertToCardListResponseList(relatedCards);
    }

    /**
     * 지식 카드 존재 여부 확인
     * sourceId와 sourceType으로 해당 UUID로 이미 지식 카드가 생성되었는지 확인
     *
     * @param sourceId   출처 ID (QNA 질문 ID, CHAT 세션 ID, BATTLE ID 등)
     * @param sourceType 출처 타입 (QNA, CHAT, BATTLE)
     * @return 지식 카드 존재 여부, 카드 ID, slug를 포함한 응답
     */
    @Transactional(readOnly = true)
    public KnowledgeCardExistsResponse checkCardExists(UUID sourceId, SourceType sourceType) {
        log.info("지식 카드 존재 여부 확인: sourceId={}, sourceType={}", sourceId, sourceType);

        Optional<KnowledgeCard> existingCard = cardRepository.findBySourceTypeAndSourceId(sourceType, sourceId);

        if (existingCard.isPresent()) {
            KnowledgeCard card = existingCard.get();
            log.info("지식 카드 존재: sourceId={}, sourceType={}, cardId={}, slug={}",
                    sourceId, sourceType, card.getId(), card.getSlug());
            return KnowledgeCardExistsResponse.builder()
                    .exists(true)
                    .cardId(card.getId())
                    .slug(card.getSlug())
                    .build();
        } else {
            log.info("지식 카드 없음: sourceId={}, sourceType={}", sourceId, sourceType);
            return KnowledgeCardExistsResponse.builder()
                    .exists(false)
                    .cardId(null)
                    .slug(null)
                    .build();
        }
    }

}