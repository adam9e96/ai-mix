package com.aimix_aimixapi.knowledge.service;

import com.aimix_aimixapi.common.exception.domain.knowledge.gpt.CardGenerationFailedException;
import com.aimix_aimixapi.common.exception.domain.knowledge.gpt.CardParsingFailedException;
import com.aimix_aimixapi.knowledge.message.KnowledgeMessage;
import com.aimix_aimixapi.gpt.dto.GptMessage;
import com.aimix_aimixapi.gpt.dto.GptMessageRole;
import com.aimix_aimixapi.gpt.service.GptService;
import com.aimix_aimixapi.common.util.JsonUtils;
import com.aimix_aimixapi.knowledge.config.KnowledgeProperties;
import com.aimix_aimixapi.knowledge.dto.GptCardResponse;
import com.aimix_aimixapi.knowledge.dto.KnowledgeCardCreateRequest;
import com.aimix_aimixapi.knowledge.entity.SourceType;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 지식 카드 GPT 서비스
 * GPT API를 사용하여 텍스트 내용에서 개념 카드를 생성하는 로직을 담당합니다.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class KnowledgeCardGptService {

    private final GptService gptService;
    private final JsonUtils jsonUtils;
    private final KnowledgeProperties knowledgeProperties;

    /**
     * GPT API를 사용하여 텍스트 내용에서 개념 카드 생성
     * CHAT와 QNA 모두에서 재사용 가능한 일반화된 메서드입니다.
     *
     * @param textContent 텍스트 내용 (대화 내용 또는 QnA 데이터, null 불가)
     * @param sourceId    출처 ID (세션 ID 또는 질문 ID, null 불가)
     * @param sourceType  출처 타입 (CHAT 또는 QNA, null 불가)
     * @param user        사용자 (토큰 사용량 기록용, null 불가)
     * @return 생성된 카드 요청 DTO
     * @throws CardGenerationFailedException GPT API 호출 실패 시
     * @throws CardParsingFailedException GPT 응답 파싱 실패 시
     * @apiNote 점검O
     * @since 2025-12-29
     */
    public KnowledgeCardCreateRequest generateCardFromText(
            String textContent, java.util.UUID sourceId, SourceType sourceType, User user) {
        log.info("GPT를 사용한 카드 생성 요청: sourceType={}, sourceId={}, textLength={}",
                sourceType, sourceId, textContent != null ? textContent.length() : 0);

        try {
            // System Message: 카드 생성 지침
            String systemMessage = knowledgeProperties.getGptPrompt().getSystemMessage();

            // User Message: 텍스트 내용
            String contentLabel = sourceType == SourceType.CHAT
                    ? knowledgeProperties.getGptPrompt().getContentLabel().getChat()
                    : knowledgeProperties.getGptPrompt().getContentLabel().getQna();
            String userMessageTemplate = knowledgeProperties.getGptPrompt().getUserMessageTemplate();
            String userMessage = String.format(userMessageTemplate, contentLabel, textContent);

            log.debug("GPT API 호출: systemMessageLength={}, userMessageLength={}",
                    systemMessage.length(), userMessage.length());

            // GPT API 호출
            List<GptMessage> messages = List.of(
                    GptMessage.builder()
                            .role(GptMessageRole.SYSTEM)
                            .content(systemMessage)
                            .build(),
                    GptMessage.builder()
                            .role(GptMessageRole.USER)
                            .content(userMessage)
                            .build()
            );

            // 더 긴 응답을 받기 위해 최대 토큰 수 증가
            // 토큰 사용량 추적을 위해 사용자 정보 전달
            String response = gptService.callGptApiWithMessages(
                    user, messages, 0.7, 2000, com.aimix_aimixapi.gpt.token.entity.GptUsageType.KNOWLEDGE_CARD);
            log.info("GPT API 응답 수신: responseLength={}", response.length());

            // JSON 응답 파싱
            return parseCardFromGptResponse(response, sourceId, sourceType);

        } catch (CardParsingFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("GPT API 호출 실패: sourceType={}, sourceId={}", sourceType, sourceId, e);
            throw new CardGenerationFailedException(
                    KnowledgeMessage.CARD_GENERATION_FAILED.format(e.getMessage()), e);
        }
    }

    /**
     * GPT 응답을 파싱하여 KnowledgeCardCreateRequest 생성
     * JSON 응답을 파싱하고 필수 필드를 검증하며 기본값을 설정합니다.
     *
     * @param gptResponse GPT API 응답 문자열 (null 불가)
     * @param sourceId    출처 ID (null 불가)
     * @param sourceType  출처 타입 (null 불가)
     * @return 파싱된 카드 생성 요청 DTO
     * @throws CardParsingFailedException 파싱 실패 시
     * @apiNote 점검O
     * @since 2025-12-29
     */
    private KnowledgeCardCreateRequest parseCardFromGptResponse(
            String gptResponse, UUID sourceId, SourceType sourceType) {
        log.debug("GPT 응답 파싱 시작: responseLength={}", gptResponse.length());

        try {
            // GPT 응답에서 JSON 추출 및 DTO로 파싱
            GptCardResponse gptCardResponse = jsonUtils.fromGptResponse(gptResponse, GptCardResponse.class);

            // 필수 필드 검증 및 기본값 설정
            String title = validateAndGetTitle(gptCardResponse);
            String oneLineDefinition = validateAndGetOneLineDefinition(gptCardResponse);
            List<String> corePoints = validateAndGetCorePoints(gptCardResponse);
            List<String> commonMistakes = validateAndGetCommonMistakes(gptCardResponse);

            KnowledgeCardCreateRequest request = KnowledgeCardCreateRequest.builder()
                    .title(title)
                    .oneLineDefinition(oneLineDefinition)
                    .corePoints(corePoints)
                    .commonMistakes(commonMistakes)
                    .sourceType(sourceType)
                    .sourceId(sourceId)
                    .build();

            log.debug("GPT 응답 파싱 완료: title={}, corePointsCount={}",
                    title, corePoints.size());

            return request;

        } catch (Exception e) {
            log.error("GPT 응답 파싱 실패: responseLength={}", gptResponse.length(), e);
            throw new CardParsingFailedException(
                    KnowledgeMessage.CARD_PARSING_FAILED.format(e.getMessage()), e);
        }
    }

    /**
     * 제목 검증 및 기본값 설정
     */
    private String validateAndGetTitle(GptCardResponse gptCardResponse) {
        if (gptCardResponse.getTitle() != null && !gptCardResponse.getTitle().trim().isEmpty()) {
            return gptCardResponse.getTitle();
        }
        return knowledgeProperties.getGptPrompt().getDefaultValues().getTitle();
    }

    /**
     * 한 줄 정의 검증 및 기본값 설정
     */
    private String validateAndGetOneLineDefinition(GptCardResponse gptCardResponse) {
        if (gptCardResponse.getOneLineDefinition() != null
                && !gptCardResponse.getOneLineDefinition().trim().isEmpty()) {
            return gptCardResponse.getOneLineDefinition();
        }
        return knowledgeProperties.getGptPrompt().getDefaultValues().getOneLineDefinition();
    }

    /**
     * 핵심 포인트 검증 및 기본값 설정
     */
    private List<String> validateAndGetCorePoints(GptCardResponse gptCardResponse) {
        List<String> corePoints = gptCardResponse.getCorePoints();
        if (corePoints == null || corePoints.isEmpty()) {
            corePoints = new ArrayList<>();
            corePoints.add(knowledgeProperties.getGptPrompt().getDefaultValues().getCorePoint());
        }
        return corePoints;
    }

    /**
     * 자주 틀리는 오해 검증 (선택 필드)
     */
    private List<String> validateAndGetCommonMistakes(GptCardResponse gptCardResponse) {
        List<String> commonMistakes = gptCardResponse.getCommonMistakes();
        if (commonMistakes != null && commonMistakes.isEmpty()) {
            return null;
        }
        return commonMistakes;
    }
}
