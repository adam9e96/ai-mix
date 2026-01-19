package com.aimix_aimixapi.gpt.token.mapper;

import com.aimix_aimixapi.gpt.token.dto.GptTokenUsageResponse;
import com.aimix_aimixapi.gpt.token.entity.GptTokenUsage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GPT 토큰 사용량 Entity ↔ DTO 변환 매퍼
 * Entity와 DTO 간의 변환 로직을 담당하는 매퍼 클래스
 */
public class GptTokenUsageMapper {

    /**
     * GptTokenUsage Entity를 DailyUsage DTO로 변환
     *
     * @param usage 변환할 Entity
     * @return DailyUsage DTO
     */
    public static GptTokenUsageResponse.DailyUsage toDailyUsage(GptTokenUsage usage) {
        if (usage == null) {
            return null;
        }

        return GptTokenUsageResponse.DailyUsage.builder()
                .date(usage.getUsageDate())
                .usageType(usage.getUsageType().name())
                .model(usage.getModel())
                .promptTokens(usage.getPromptTokens())
                .completionTokens(usage.getCompletionTokens())
                .totalTokens(usage.getTotalTokens())
                .requestCount(usage.getRequestCount())
                .isUserApiKey(usage.getIsUserApiKey())
                .build();
    }

    /**
     * GptTokenUsage Entity 리스트를 DailyUsage DTO 리스트로 변환
     *
     * @param usageList 변환할 Entity 리스트
     * @return DailyUsage DTO 리스트
     */
    public static List<GptTokenUsageResponse.DailyUsage> toDailyUsageList(List<GptTokenUsage> usageList) {
        if (usageList == null) {
            return List.of();
        }

        return usageList.stream()
                .map(GptTokenUsageMapper::toDailyUsage)
                .collect(Collectors.toList());
    }
}
