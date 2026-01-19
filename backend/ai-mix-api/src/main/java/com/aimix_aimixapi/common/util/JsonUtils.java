package com.aimix_aimixapi.common.util;

import com.aimix_aimixapi.gpt.util.GptResponseUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JSON 처리 유틸리티
 * - ObjectMapper를 래핑하여 공통적인 JSON 처리를 담당
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    /**
     * 객체를 JSON 문자열로 변환
     */
    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패: {}", e.getMessage(), e);
            throw new RuntimeException("JSON 변환에 실패했습니다.", e);
        }
    }

    /**
     * JSON 문자열을 객체로 변환
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("JSON 파싱에 실패했습니다.", e);
        }
    }

    /**
     * JSON 문자열을 리스트로 변환
     */
    public <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.error("JSON 리스트 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("JSON 리스트 파싱에 실패했습니다.", e);
        }
    }

    /**
     * TypeReference를 사용하여 JSON 문자열을 객체로 변환
     * 
     * @param json JSON 문자열
     * @param typeReference 타입 참조 (예: new TypeReference<List<String>>() {})
     * @return 파싱된 객체
     * @throws RuntimeException JSON 파싱 실패 시
     */
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("JSON 파싱에 실패했습니다.", e);
        }
    }

    /**
     * GPT 응답에서 JSON을 추출하고 객체로 변환
     * GPT 응답은 마크다운 코드 블록으로 감싸져 있을 수 있으므로,
     * GptResponseUtils를 사용하여 JSON을 추출한 후 파싱합니다.
     * 
     * @param gptResponse GPT API 응답 문자열
     * @param clazz 변환할 클래스
     * @return 파싱된 객체
     * @throws RuntimeException JSON 추출 또는 파싱 실패 시
     */
    public <T> T fromGptResponse(String gptResponse, Class<T> clazz) {
        String jsonText = GptResponseUtils.extractJsonFromResponse(gptResponse);
        return fromJson(jsonText, clazz);
    }

    /**
     * GPT 응답에서 JSON을 추출하고 TypeReference를 사용하여 객체로 변환
     * 
     * @param gptResponse GPT API 응답 문자열
     * @param typeReference 타입 참조 (예: new TypeReference<List<String>>() {})
     * @return 파싱된 객체
     * @throws RuntimeException JSON 추출 또는 파싱 실패 시
     */
    public <T> T fromGptResponse(String gptResponse, TypeReference<T> typeReference) {
        String jsonText = GptResponseUtils.extractJsonFromResponse(gptResponse);
        return fromJson(jsonText, typeReference);
    }
}
