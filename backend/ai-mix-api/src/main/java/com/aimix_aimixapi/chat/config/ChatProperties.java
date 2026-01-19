package com.aimix_aimixapi.chat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 채팅 서비스 관련 설정
 * chat-config.yml에서 설정값을 주입받음
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "chat")
public class ChatProperties {

    /**
     * 세션 제목 최대 길이 (자)
     * chat-config.yml에서 설정
     */
    private int maxTitleLength;

    /**
     * 세션 제목 생략 표시 (예: "...")
     * chat-config.yml에서 설정
     */
    private String titleEllipsis;

    /**
     * 기본 세션 제목 (메시지가 없을 때 사용)
     * chat-config.yml에서 설정
     */
    private String defaultTitle;
}
