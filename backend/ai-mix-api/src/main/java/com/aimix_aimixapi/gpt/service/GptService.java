package com.aimix_aimixapi.gpt.service;

import com.aimix_aimixapi.gpt.config.ChatGptProperties;
import com.aimix_aimixapi.gpt.dto.GptApiKeyInfo;
import com.aimix_aimixapi.gpt.dto.GptMessage;
import com.aimix_aimixapi.gpt.dto.GptMessageRole;
import com.aimix_aimixapi.gpt.dto.GptTokenUsage;
import com.aimix_aimixapi.gpt.token.entity.GptUsageType;
import com.aimix_aimixapi.gpt.token.service.record.GptTokenUsageRecordService;
import com.aimix_aimixapi.user.entity.User;
import com.aimix_aimixapi.user.service.UserApiKeyService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.errors.RateLimitException;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 공통 GPT 서비스
 * - ChatGPT API 호출 로직을 공통화
 * - 여러 패키지에서 재사용 가능
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GptService {

    private final ChatGptProperties chatGptProperties;
    private final GptTokenUsageRecordService recordService;
    private final UserApiKeyService userApiKeyService;

    /**
     * GPT API 호출 (단일 메시지)
     *
     * @param prompt 사용자 프롬프트
     * @return GPT 응답
     * @apiNote 점검O
     * @since 2026-01-05
     */
    public String callGptApi(String prompt) {
        return callGptApi(prompt, chatGptProperties.getDefaultTemperature(), chatGptProperties.getDefaultMaxTokens());
    }

    /**
     * GPT API 호출 (단일 메시지, 커스텀 파라미터, 사용자 정보 포함)
     * 토큰 사용량을 기록합니다.
     *
     * @param user        사용자 (토큰 사용량 기록용, null 가능)
     * @param prompt      사용자 프롬프트
     * @param temperature 온도 설정 (0.0 ~ 2.0), null이면 기본값 사용
     * @param maxTokens   최대 토큰 수, null이면 기본값 사용
     * @param usageType   사용 유형 (토큰 사용량 기록용)
     * @return GPT 응답
     */
    public String callGptApi(User user, String prompt, Double temperature, Integer maxTokens, GptUsageType usageType) {
        ChatCompletion completion = callGptApiInternal(user, prompt, temperature, maxTokens, null, usageType);
        return extractResponse(completion);
    }

    /**
     * GPT API 호출 (단일 메시지, 커스텀 파라미터)
     *
     * @param prompt      사용자 프롬프트
     * @param temperature 온도 설정 (0.0 ~ 2.0), null이면 기본값 사용
     * @param maxTokens   최대 토큰 수, null이면 기본값 사용
     * @return GPT 응답
     */
    public String callGptApi(String prompt, Double temperature, Integer maxTokens) {
        ChatCompletion completion = callGptApiInternal(null, prompt, temperature, maxTokens, null, null);
        return extractResponse(completion);
    }

    /**
     * GPT API 호출 (대화형 - 메시지 리스트)
     *
     * @param messages 대화 메시지 리스트 (각 메시지는 "role: content" 형식 또는 Message 객체)
     * @return GPT 응답
     */
    public String callGptApiWithMessages(List<GptMessage> messages) {
        return callGptApiWithMessages(messages, null, null);
    }

    /**
     * GPT API 호출 (대화형 - 메시지 리스트, 커스텀 파라미터, 사용자 정보 포함)
     * 토큰 사용량을 기록합니다.
     *
     * @param user        사용자 (토큰 사용량 기록용, null 가능)
     * @param messages    대화 메시지 리스트
     * @param temperature 온도 설정 (0.0 ~ 2.0), null이면 기본값 사용
     * @param maxTokens   최대 토큰 수, null이면 기본값 사용
     * @param usageType   사용 유형 (토큰 사용량 기록용)
     * @return GPT 응답
     */
    public String callGptApiWithMessages(User user, List<GptMessage> messages, Double temperature, Integer maxTokens, GptUsageType usageType) {
        ChatCompletion completion = callGptApiInternal(user, null, temperature, maxTokens, messages, usageType);
        return extractResponse(completion);
    }

    /**
     * GPT API 호출 (대화형 - 메시지 리스트, 커스텀 파라미터)
     *
     * @param messages 대화 메시지 리스트
     * @param temperature 온도 설정 (0.0 ~ 2.0), null이면 기본값 사용
     * @param maxTokens 최대 토큰 수, null이면 기본값 사용
     * @return GPT 응답
     */
    public String callGptApiWithMessages(List<GptMessage> messages, Double temperature, Integer maxTokens) {
        ChatCompletion completion = callGptApiInternal(null, null, temperature, maxTokens, messages, null);
        return extractResponse(completion);
    }

    /**
     * GPT API 호출 내부 메서드 (공통 로직)
     * - API 키 선택 및 검증
     * - OpenAI 클라이언트 생성
     * - 파라미터 빌드
     * - API 호출 실행
     * - 토큰 사용량 기록
     * - 예외 처리
     *
     * @param user        사용자 (토큰 사용량 기록용, null 가능)
     * @param prompt      단일 프롬프트 (messages가 null일 때 사용)
     * @param temperature 온도 설정 (0.0 ~ 2.0), null이면 기본값 사용
     * @param maxTokens   최대 토큰 수, null이면 기본값 사용
     * @param messages    대화 메시지 리스트 (prompt가 null일 때 사용)
     * @param usageType   사용 유형 (토큰 사용량 기록용, null 가능)
     * @return ChatCompletion 응답 객체
     * @throws RuntimeException API 호출 실패 시
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private ChatCompletion callGptApiInternal(User user, String prompt, Double temperature, Integer maxTokens, List<GptMessage> messages, GptUsageType usageType) {
        try {
            // 1. API 키 선택 및 검증
            GptApiKeyInfo apiKeyInfo = selectAndValidateApiKey(user);
            
            // 2. OpenAI 클라이언트 생성
            OpenAIClient client = createOpenAIClient(apiKeyInfo.getApiKey());
            
            // 3. 파라미터 빌드
            ChatCompletionCreateParams params = buildChatParams(prompt, messages, temperature, maxTokens);
            
            // 4. API 호출
            ChatCompletion completion = executeApiCall(client, params);
            
            // 5. 토큰 사용량 기록
            recordTokenUsageIfNeeded(user, completion, usageType, apiKeyInfo.isUserApiKey());
            
            return completion;
            
        } catch (RateLimitException e) {
            handleRateLimitException(user, e);
            return null; // unreachable
        } catch (Exception e) {
            handleException(user, e);
            return null; // unreachable
        }
    }

    /**
     * 예외 처리 (런타임 RateLimit 체크 포함)
     * - RateLimitException인지 확인하여 적절한 핸들러 호출
     *
     * @param user 사용자 (null 가능)
     * @param e    발생한 예외
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void handleException(User user, Exception e) {
        if (isRateLimitException(e)) {
            handleRateLimitException(user, e);
        } else {
            handleGeneralException(user, e);
        }
    }

    /**
     * RateLimitException 여부 확인
     * - 클래스 이름으로 RateLimit 포함 여부 확인
     *
     * @param e 확인할 예외
     * @return RateLimitException이면 true
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private boolean isRateLimitException(Exception e) {
        return e.getClass().getSimpleName().contains("RateLimit");
    }


    /**
     * API 키 선택 및 검증
     * - 사용자 API 키가 있으면 우선 사용
     * - 없으면 기본 API 키 사용
     * - 사용자 API 키 사용 여부 확인
     *
     * @param user 사용자 (null 가능)
     * @return API 키 정보 (키 값과 사용자 키 여부)
     * @throws RuntimeException 기본 API 키가 설정되지 않은 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private GptApiKeyInfo selectAndValidateApiKey(User user) {
        String apiKey = selectApiKey(user);
        boolean isUserApiKey = determineIfUserApiKey(user, apiKey);
        return new GptApiKeyInfo(apiKey, isUserApiKey);
    }

    /**
     * 사용자 API 키인지 확인
     * - 사용자가 없거나 API 키가 null이면 false
     * - 기본 키와 다르면 사용자 키로 간주
     * - 추가 검증: 사용자 API 키가 등록되어 있고 일치하는지 확인
     *
     * @param user   사용자 (null 가능)
     * @param apiKey 확인할 API 키
     * @return 사용자 API 키이면 true, 기본 키이면 false
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private boolean determineIfUserApiKey(User user, String apiKey) {
        if (user == null || apiKey == null) {
            return false;
        }
        
        String defaultKey = chatGptProperties.getApiKey();
        // 기본 키와 다르면 사용자 키로 간주
        if (defaultKey != null && !apiKey.equals(defaultKey)) {
            log.debug("사용자 API 키 사용 확인: userId={}, isUserApiKey=true", user.getId());
            return true;
        }
        
        // 추가 검증: 사용자 API 키가 등록되어 있는지 확인
        try {
            if (userApiKeyService.hasApiKey(user) && 
                apiKey.equals(userApiKeyService.getApiKey(user))) {
                log.debug("사용자 API 키 사용 확인 (추가 검증): userId={}, isUserApiKey=true", user.getId());
                return true;
            }
        } catch (Exception e) {
            log.debug("사용자 API 키 확인 실패, 공용 키로 간주: userId={}", user.getId());
        }
        
        return false;
    }

    /**
     * OpenAI 클라이언트 생성
     * - API 키를 사용하여 OpenAI HTTP 클라이언트 생성
     *
     * @param apiKey OpenAI API 키
     * @return 생성된 OpenAI 클라이언트
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private OpenAIClient createOpenAIClient(String apiKey) {
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * 파라미터 빌드
     * - 메시지 리스트가 있으면 메시지 방식으로 빌드
     * - 없으면 단일 프롬프트 방식으로 빌드
     * - temperature와 maxTokens는 null이면 기본값 사용
     *
     * @param prompt      단일 프롬프트 (messages가 null일 때 사용)
     * @param messages    대화 메시지 리스트 (null 가능)
     * @param temperature 온도 설정 (0.0 ~ 2.0), null이면 기본값 사용
     * @param maxTokens   최대 토큰 수, null이면 기본값 사용
     * @return ChatCompletion 파라미터 객체
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private ChatCompletionCreateParams buildChatParams(String prompt, List<GptMessage> messages,
            Double temperature, Integer maxTokens) {
        ChatModel model = parseChatModel(chatGptProperties.getDefaultModel());
        Double finalTemperature = temperature != null ? temperature : chatGptProperties.getDefaultTemperature();
        Integer finalMaxTokens = maxTokens != null ? maxTokens : chatGptProperties.getDefaultMaxTokens();
        
        if (messages != null) {
            return buildMessageParams(model, messages, finalTemperature, finalMaxTokens);
        } else {
            return buildPromptParams(model, prompt, finalTemperature, finalMaxTokens);
        }
    }

    /**
     * 메시지 리스트 방식 파라미터 빌드
     * - 대화형 GPT API 호출을 위한 파라미터 생성
     * - USER, AI, SYSTEM 역할에 따라 메시지 추가
     *
     * @param model       ChatModel enum
     * @param messages    대화 메시지 리스트
     * @param temperature 온도 설정 (0.0 ~ 2.0)
     * @param maxTokens   최대 토큰 수
     * @return ChatCompletion 파라미터 객체
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private ChatCompletionCreateParams buildMessageParams(ChatModel model, List<GptMessage> messages,
            Double temperature, Integer maxTokens) {
        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(model)
                .temperature(temperature)
                .maxCompletionTokens(maxTokens);

        // 메시지 추가
        for (GptMessage msg : messages) {
            addMessageByRole(paramsBuilder, msg);
        }

        ChatCompletionCreateParams params = paramsBuilder.build();
        log.info("GPT API 호출 (메시지 리스트): model={}, temperature={}, maxTokens={}, messageCount={}",
                model, temperature, maxTokens, messages.size());
        return params;
    }

    /**
     * 역할에 따라 메시지 추가
     * - USER, AI, SYSTEM 역할에 따라 적절한 메시지 타입으로 추가
     *
     * @param paramsBuilder ChatCompletion 파라미터 빌더
     * @param msg           추가할 메시지
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void addMessageByRole(ChatCompletionCreateParams.Builder paramsBuilder, GptMessage msg) {
        GptMessageRole role = msg.getRole();
        if (role == GptMessageRole.USER) {
            paramsBuilder.addUserMessage(msg.getContent());
        } else if (role == GptMessageRole.AI) {
            paramsBuilder.addAssistantMessage(msg.getContent());
        } else if (role == GptMessageRole.SYSTEM) {
            paramsBuilder.addSystemMessage(msg.getContent());
        }
    }

    /**
     * 단일 프롬프트 방식 파라미터 빌드
     * - 단일 메시지 GPT API 호출을 위한 파라미터 생성
     *
     * @param model       ChatModel enum
     * @param prompt      사용자 프롬프트
     * @param temperature 온도 설정 (0.0 ~ 2.0)
     * @param maxTokens   최대 토큰 수
     * @return ChatCompletion 파라미터 객체
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private ChatCompletionCreateParams buildPromptParams(ChatModel model, String prompt,
            Double temperature, Integer maxTokens) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .temperature(temperature)
                .maxCompletionTokens(maxTokens)
                .addUserMessage(prompt)
                .build();

        log.info("GPT API 호출: model={}, temperature={}, maxTokens={}, promptLength={}",
                model, temperature, maxTokens, prompt != null ? prompt.length() : 0);
        return params;
    }

    /**
     * API 호출 실행
     * - OpenAI 클라이언트를 사용하여 ChatCompletion API 호출
     *
     * @param client OpenAI 클라이언트
     * @param params ChatCompletion 파라미터
     * @return ChatCompletion 응답 객체
     * @throws RuntimeException API 호출 실패 시
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private ChatCompletion executeApiCall(OpenAIClient client, ChatCompletionCreateParams params) {
        return client.chat().completions().create(params);
    }

    /**
     * 토큰 사용량 기록 (필요한 경우)
     * - 사용자 정보와 usageType이 모두 제공된 경우에만 기록
     *
     * @param user         사용자 (null 가능)
     * @param completion   ChatCompletion 응답
     * @param usageType    사용 유형 (null 가능)
     * @param isUserApiKey 사용자 API 키 사용 여부
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void recordTokenUsageIfNeeded(User user, ChatCompletion completion, GptUsageType usageType, boolean isUserApiKey) {
        if (user != null && usageType != null) {
            recordTokenUsage(user, completion, usageType, isUserApiKey);
        }
    }

    /**
     * RateLimitException 처리
     * - API 할당량 초과 예외를 사용자 친화적인 메시지로 변환
     *
     * @param user 사용자 (null 가능, 에러 메시지에 사용)
     * @param e    RateLimitException 또는 RateLimit 관련 예외
     * @throws RuntimeException 항상 예외를 던짐 (에러 메시지 포함)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void handleRateLimitException(User user, Exception e) {
        String errorMessage = "API 할당량을 초과했습니다. " + 
                (user != null ? "등록하신 API 키의 할당량을 확인해주세요." : "기본 API 키의 할당량을 확인해주세요.");
        log.error("GPT API 호출 실패 (할당량 초과): userId={}, error={}", 
                user != null ? user.getId() : null, e.getMessage());
        throw new RuntimeException(errorMessage, e);
    }

    /**
     * 일반 예외 처리
     * - RateLimitException이 아닌 일반 예외 처리
     * - 로그 기록 후 RuntimeException으로 래핑하여 재던짐
     *
     * @param user 사용자 (null 가능, 로그에 사용)
     * @param e    발생한 예외
     * @throws RuntimeException 항상 예외를 던짐 (에러 메시지 포함)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private void handleGeneralException(User user, Exception e) {
        log.error("GPT API 호출 실패: userId={}, error={}", 
                user != null ? user.getId() : null, e.getMessage(), e);
        throw new RuntimeException("GPT API 호출에 실패했습니다: " + e.getMessage(), e);
    }

    /**
     * ChatCompletion에서 응답 텍스트 추출
     * - ChatCompletion 객체에서 실제 응답 텍스트를 추출
     * - choices가 비어있으면 예외 발생
     *
     * @param completion ChatCompletion 응답 객체
     * @return 추출된 응답 텍스트
     * @throws RuntimeException choices가 비어있거나 응답을 생성할 수 없는 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private String extractResponse(ChatCompletion completion) {
        if (completion.choices().isEmpty()) {
            log.warn("GPT API 응답이 비어있습니다");
            throw new RuntimeException("GPT API 응답이 비어있습니다");
        }

        String response = completion.choices().getFirst().message().content()
                .orElse("응답을 생성할 수 없습니다.");

        log.info("GPT API 응답 수신: responseLength={}", response.length());
        return response;
    }

    /**
     * 토큰 사용량 기록
     * - ChatCompletion에서 usage 정보를 추출하여 기록
     * - Reflection을 사용하여 토큰 사용량 추출 (promptTokens, completionTokens, totalTokens)
     * - 기록 실패 시에도 API 호출 자체는 실패하지 않음
     *
     * @param user         사용자 (null 불가)
     * @param completion   ChatCompletion 응답 객체
     * @param usageType    사용 유형 (CHAT, QNA, BATTLE_QUESTION 등)
     * @param isUserApiKey 사용자 API 키 사용 여부
     * @apiNote 점검O
     * @since 2025-12-10
     */
    private void recordTokenUsage(User user, ChatCompletion completion, GptUsageType usageType, boolean isUserApiKey) {
        try {
            // ChatCompletion에서 usage 정보 추출
            Optional<?> usageOpt = completion.usage();
            
            if (usageOpt.isEmpty()) {
                log.warn("GPT API 응답에 usage 정보가 없습니다: userId={}, type={}, completionId={}", 
                        user.getId(), usageType, completion.id());
                return;
            }

            Object usage = usageOpt.get();
            log.debug("Usage 객체 타입: {}", usage.getClass().getName());
            
            // reflection을 사용하여 토큰 사용량 추출
            GptTokenUsage tokenUsage = extractTokenUsage(usage);
            if (tokenUsage == null) {
                return;
            }

            Integer promptTokens = tokenUsage.getPromptTokens();
            Integer completionTokens = tokenUsage.getCompletionTokens();
            Integer totalTokens = tokenUsage.getTotalTokens();

            // 토큰 사용량이 모두 0인 경우 경고
            if (isAllTokensZero(promptTokens, completionTokens, totalTokens)) {
                log.warn("토큰 사용량이 모두 0입니다: userId={}, type={}, usageClass={}", 
                        user.getId(), usageType, usage.getClass().getName());
            }

            // 모델명 추출
            completion.model();
            String model = completion.model();

            log.info("토큰 사용량 추출: userId={}, type={}, model={}, promptTokens={}, completionTokens={}, totalTokens={}",
                    user.getId(), usageType, model, promptTokens, completionTokens, totalTokens);

            // 토큰 사용량 기록
            recordService.recordTokenUsage(
                    user,
                    usageType,
                    model,
                    promptTokens,
                    completionTokens,
                    totalTokens,
                    isUserApiKey
            );

            log.info("토큰 사용량 기록 완료: userId={}, type={}, totalTokens={}",
                    user.getId(), usageType, totalTokens);

        } catch (Exception e) {
            log.error("토큰 사용량 기록 실패: userId={}, type={}, error={}", 
                    user.getId(), usageType, e.getMessage(), e);
            // 토큰 사용량 기록 실패는 API 호출 자체를 실패시키지 않음
        }
    }


    /**
     * Usage 객체에서 토큰 사용량 추출
     * - Reflection을 사용하여 promptTokens, completionTokens, totalTokens 추출
     *
     * @param usage Usage 객체
     * @return 토큰 사용량 정보 (추출 실패 시 null)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private GptTokenUsage extractTokenUsage(Object usage) {
        try {
            Integer promptTokens = extractTokenValue(usage, "promptTokens");
            Integer completionTokens = extractTokenValue(usage, "completionTokens");
            Integer totalTokens = extractTokenValue(usage, "totalTokens");

            log.debug("토큰 사용량 추출 성공: promptTokens={}, completionTokens={}, totalTokens={}",
                    promptTokens, completionTokens, totalTokens);

            return new GptTokenUsage(promptTokens, completionTokens, totalTokens);

        } catch (NoSuchMethodException e) {
            log.error("Usage 객체에서 메서드를 찾을 수 없습니다: usageClass={}, error={}", 
                    usage.getClass().getName(), e.getMessage());
            // 사용 가능한 메서드 목록 로깅
            log.error("사용 가능한 메서드: {}", 
                    java.util.Arrays.stream(usage.getClass().getMethods())
                            .map(java.lang.reflect.Method::getName)
                            .collect(java.util.stream.Collectors.joining(", ")));
            return null;
        } catch (Exception e) {
            log.error("토큰 사용량 추출 중 오류 발생: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Reflection을 사용하여 토큰 값 추출
     *
     * @param usage    Usage 객체
     * @param methodName 메서드 이름 (promptTokens, completionTokens, totalTokens)
     * @return 추출된 토큰 값 (추출 실패 시 0)
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private Integer extractTokenValue(Object usage, String methodName) throws Exception {
        java.lang.reflect.Method method = usage.getClass().getMethod(methodName);
        Object value = method.invoke(usage);
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value != null) {
            log.warn("{} 타입이 예상과 다릅니다: {}", methodName, value.getClass().getName());
        }
        
        return 0;
    }

    /**
     * 모든 토큰 값이 0인지 확인
     *
     * @param promptTokens     프롬프트 토큰 수
     * @param completionTokens 완료 토큰 수
     * @param totalTokens      전체 토큰 수
     * @return 모두 0이면 true
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private boolean isAllTokensZero(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        return promptTokens == 0 && completionTokens == 0 && totalTokens == 0;
    }

    /**
     * 주어진 모델명 문자열을 {@link ChatModel} enum으로 변환합니다.
     * <p>
     * - 입력이 {@code null} 이거나 공백이면 기본값으로 {@link ChatModel#GPT_4O_MINI} 를 반환합니다.<br>
     * - 소문자로 normalize 하여 비교합니다.<br>
     * - 인식되지 않는 모델명일 경우 경고 로그를 출력한 뒤 기본값을 반환합니다.
     *
     * @param modelName 변환할 모델명 문자열
     * @return 변환된 {@link ChatModel} 값, 또는 기본값 {@link ChatModel#GPT_4O_MINI}
     * @since 2025-12-10
     */
    public ChatModel parseChatModel(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return ChatModel.GPT_4O_MINI;
        }

        String normalized = modelName.toLowerCase().trim().replace("_", "-");
        return switch (normalized) {
            case "gpt-4o-mini", "gpt4o-mini" -> ChatModel.GPT_4O_MINI;
            case "gpt-5-nano" -> ChatModel.GPT_5_NANO;
            default -> {
                log.warn("알 수 없는 모델명: {}, 기본값 GPT_4O_MINI 사용", modelName);
                yield ChatModel.GPT_4O_MINI;
            }
        };
    }

    /**
     * API 키 선택 로직
     * 사용자 API 키가 있으면 사용, 없으면 기본 API 키 사용
     * 
     * @param user 사용자 (null 가능)
     * @return 선택된 API 키
     * @throws RuntimeException 기본 API 키가 설정되지 않은 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private String selectApiKey(User user) {
        // 사용자가 없으면 기본 키 사용
        if (user == null) {
            log.debug("사용자 정보 없음, 기본 키 사용");
            return getDefaultApiKey();
        }
        
        // 사용자 API 키 조회 시도
        String userApiKey = tryGetUserApiKey(user);
        if (userApiKey != null) {
            return userApiKey;
        }
        
        // 사용자 API 키를 사용할 수 없으면 기본 키 사용
        return getDefaultApiKey();
    }

    /**
     * 사용자 API 키 조회 시도
     * - 사용자 API 키가 등록되어 있고 유효하면 반환
     * - 실패하면 null 반환 (예외는 내부에서 처리)
     *
     * @param user 사용자 (null 불가)
     * @return 사용자 API 키 (사용 가능한 경우), 사용 불가능하면 null
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private String tryGetUserApiKey(User user) {
        try {
            boolean hasApiKey = userApiKeyService.hasApiKey(user);
            if (!hasApiKey) {
                log.debug("사용자 API 키 없음, 기본 키 사용: userId={}", user.getId());
                return null;
            }
            
            String userApiKey = userApiKeyService.getApiKey(user);
            if (userApiKey == null || userApiKey.isEmpty()) {
                log.warn("⚠️ 사용자 API 키가 등록되어 있지만 조회 실패: userId={}", user.getId());
                return null;
            }
            
            log.info("✅ 사용자 API 키 사용: userId={}, maskedKey={}", 
                    user.getId(), userApiKeyService.maskApiKey(userApiKey));
            return userApiKey;
            
        } catch (Exception e) {
            log.warn("사용자 API 키 조회 실패, 기본 키 사용: userId={}, error={}", 
                    user.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 기본 API 키 조회
     * - 설정에서 기본 API 키를 가져옴
     * - 키가 없으면 예외 발생
     *
     * @return 기본 API 키
     * @throws RuntimeException 기본 API 키가 설정되지 않은 경우
     * @apiNote 점검O
     * @since 2026-01-05
     */
    private String getDefaultApiKey() {
        String defaultKey = chatGptProperties.getApiKey();
        if (defaultKey == null || defaultKey.isEmpty()) {
            log.error("기본 API 키가 설정되지 않았습니다");
            throw new RuntimeException("OpenAI API 키가 설정되지 않았습니다");
        }
        
        log.info("기본 API 키 사용 (사용자 API 키 없음)");
        return defaultKey;
    }

}
