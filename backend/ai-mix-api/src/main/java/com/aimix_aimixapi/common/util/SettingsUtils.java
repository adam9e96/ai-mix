package com.aimix_aimixapi.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 설정 유틸리티
 * - Settings JSON 문자열 파싱
 * - 기본 설정 제공
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class SettingsUtils {

    private final ObjectMapper objectMapper;

    /**
     * Settings JSON 문자열 파싱
     * - null이거나 빈 문자열인 경우 기본 설정 반환
     * - 파싱 실패 시 기본 설정 반환
     *
     * @param settingsJson Settings JSON 문자열
     * @return 파싱된 설정 Map (기본 설정 포함)
     * @apiNote 점검O
     * @since 2025-12-28
     */
    public Map<String, Object> parseSettings(String settingsJson) {
        if (!StringUtils.hasText(settingsJson)) {
            log.debug("Settings JSON이 null이거나 빈 문자열, 기본 설정 반환");
            return getDefaultSettings();
        }

        try {
            Map<String, Object> settings = objectMapper.readValue(settingsJson, new TypeReference<Map<String, Object>>() {
            });
            log.debug("Settings JSON 파싱 성공");
            return settings;
        } catch (Exception e) {
            log.warn("Settings JSON 파싱 실패: {}, 기본값 사용", settingsJson, e);
            return getDefaultSettings();
        }
    }

    /**
     * 기본 설정 반환
     *
     * @return 기본 설정 Map
     * @apiNote 점검O
     * @since 2025-12-28
     */
    private Map<String, Object> getDefaultSettings() {
        Map<String, Object> defaultSettings = new HashMap<>();
        defaultSettings.put("darkMode", false);
        defaultSettings.put("notifications", true);
        return defaultSettings;
    }
}
