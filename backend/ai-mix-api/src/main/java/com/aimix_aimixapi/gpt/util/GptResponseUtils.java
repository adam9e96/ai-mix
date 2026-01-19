package com.aimix_aimixapi.gpt.util;

import com.aimix_aimixapi.chat.entity.ChatMessage;
import com.aimix_aimixapi.chat.entity.MessageSender;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * GPT API 응답 처리 유틸리티
 * GPT API 응답에서 JSON을 추출하는 공통 기능
 * 대화 내용을 텍스트로 변환하는 공통 기능
 *
 * @since 2025-12-18
 */
public class GptResponseUtils {

    /**
     * GPT API 응답에서 JSON 부분만 추출 (마크다운 코드 블록 제거)
     * <p>GPT API 응답은 때때로 마크다운 코드 블록(```json ... ```)으로 감싸져 있을 수 있음.
     * 이 메서드는 마크다운 코드 블록을 제거하고 순수 JSON 문자열만 추출
     *
     * @param response GPT API 원본 응답
     * @return 추출된 JSON 문자열
     * @throws IllegalArgumentException 응답이 비어있는 경우
     */
    public static String extractJsonFromResponse(String response) {
        if (!StringUtils.hasText(response)) {
            throw new IllegalArgumentException("응답이 비어있습니다");
        }

        String trimmed = response.trim();

        // 마크다운 코드 블록 제거 (```json 또는 ```)
        if (trimmed.startsWith("```")) {
            // 시작 부분 찾기 (```json 또는 ```)
            int startMarkerEnd = trimmed.indexOf("\n");
            if (startMarkerEnd < 0) {
                // 줄바꿈이 없으면 ```만 있는 경우
                startMarkerEnd = trimmed.indexOf("```", 3);
                if (startMarkerEnd > 0) {
                    trimmed = trimmed.substring(3, startMarkerEnd);
                } else {
                    // 닫는 마커가 없으면 시작 마커만 제거
                    trimmed = trimmed.substring(3);
                }
            } else {
                trimmed = trimmed.substring(startMarkerEnd + 1);
            }

            // 끝 부분 찾기 (```)
            int endIdx = trimmed.lastIndexOf("```");
            if (endIdx > 0) {
                trimmed = trimmed.substring(0, endIdx);
            }
        }

        // 중괄호로 시작하는 JSON 찾기 (여러 JSON이 있는 경우 첫 번째 것 사용)
        int jsonStart = trimmed.indexOf('{');
        int jsonEnd = trimmed.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            trimmed = trimmed.substring(jsonStart, jsonEnd + 1);
        } else if (jsonStart < 0) {
            // JSON 객체가 없으면 배열 시도
            jsonStart = trimmed.indexOf('[');
            jsonEnd = trimmed.lastIndexOf(']');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                trimmed = trimmed.substring(jsonStart, jsonEnd + 1);
            }
        }

        return trimmed.trim();
    }

    /**
     * 대화 내용을 텍스트로 변환
     * ChatMessage 리스트를 "사용자: 메시지\n\nAI: 메시지\n\n" 형식의 텍스트로 변환합니다.
     * GPT API에 전달하기 전 대화 내용을 포맷팅하는 데 사용됩니다.
     *
     * @param messages ChatMessage 리스트
     * @return 포맷팅된 대화 내용 텍스트
     * @since 2025-12-18
     */
    public static String buildConversationText(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : messages) {
            String sender = message.getSender() == MessageSender.USER ? "사용자" : "AI";
            sb.append(sender).append(": ").append(message.getMessage()).append("\n\n");
        }
        return sb.toString();
    }
}
