package com.aimix_aimixapi.gpt.token.service.record;

import com.aimix_aimixapi.gpt.token.entity.GptTokenUsage;
import com.aimix_aimixapi.gpt.token.entity.GptUsageType;
import com.aimix_aimixapi.gpt.token.repository.GptTokenUsageRepository;
import com.aimix_aimixapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * GPT 토큰 사용량 기록 서비스
 * 토큰 사용량을 기록하는 서비스
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GptTokenUsageRecordService {

    private final GptTokenUsageRepository gptTokenUsageRepository;

    /**
     * 토큰 사용량 기록
     * <p>같은 날짜, 같은 유형, 같은 API 키 타입의 호출은 하나의 레코드로 집계합니다.
     *
     * <p><b>집계 방식:</b>
     * <ul>
     *   <li>같은 날짜, 같은 사용 유형, 같은 API 키 타입의 호출은 기존 레코드를 업데이트</li>
     *   <li>토큰 수와 호출 횟수를 누적하여 집계</li>
     *   <li>새로운 조합의 경우 새 레코드 생성</li>
     * </ul>
     *
     * <p><b>트랜잭션:</b>
     * <ul>
     *   <li>REQUIRES_NEW를 사용하여 readOnly 트랜잭션에서도 독립적으로 실행</li>
     *   <li>기록 작업이 조회 작업에 영향을 주지 않도록 함</li>
     * </ul>
     *
     * @param user             사용자
     * @param usageType        사용 유형
     * @param model            모델명
     * @param promptTokens     프롬프트 토큰 수
     * @param completionTokens 완료 토큰 수
     * @param totalTokens      총 토큰 수
     * @param isUserApiKey     사용자 API 키 사용 여부 (true: 사용자 키, false: 공용 키)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordTokenUsage(User user, GptUsageType usageType, String model, Integer promptTokens,
                                 Integer completionTokens, Integer totalTokens, Boolean isUserApiKey) {

        if (user == null) {
            log.warn("사용자 정보가 없어 토큰 사용량을 기록할 수 없습니다");
            return;
        }

        LocalDate today = LocalDate.now();
        // null인 경우 false로 처리 (기존 데이터 호환성)
        Boolean apiKeyType = isUserApiKey != null ? isUserApiKey : false;

        // 기존 레코드 조회 (같은 날짜, 같은 유형, 같은 API 키 타입)
        GptTokenUsage existing = gptTokenUsageRepository
                .findByUserAndUsageDateAndUsageTypeAndIsUserApiKey(user, today, usageType, apiKeyType)
                .orElse(null);

        if (existing != null) {
            // 기존 레코드 업데이트 (집계)
            existing.setPromptTokens(existing.getPromptTokens() + promptTokens);
            existing.setCompletionTokens(existing.getCompletionTokens() + completionTokens);
            existing.setTotalTokens(existing.getTotalTokens() + totalTokens);
            existing.setRequestCount(existing.getRequestCount() + 1);
            existing.setUpdatedAt(LocalDateTime.now());
            gptTokenUsageRepository.save(existing);

            log.debug("토큰 사용량 업데이트: userId={}, type={}, isUserApiKey={}, totalTokens={}",
                    user.getId(), usageType, apiKeyType, existing.getTotalTokens());
        } else {
            // 새 레코드 생성
            GptTokenUsage usage = GptTokenUsage.builder()
                    .user(user)
                    .usageDate(today)
                    .usageType(usageType)
                    .model(model)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .requestCount(1)
                    .isUserApiKey(apiKeyType)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            gptTokenUsageRepository.save(usage);

            log.info("토큰 사용량 기록: userId={}, type={}, model={}, isUserApiKey={}, totalTokens={}",
                    user.getId(), usageType, model, apiKeyType, totalTokens);
        }
    }
}
