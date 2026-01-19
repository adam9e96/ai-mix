package com.aimix_aimixapi.common.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정
 * - WebFlux 기반 비동기 HTTP 클라이언트 설정
 * - ChatGPT API 호출 등에 사용
 */
@Log4j2
@Configuration
public class WebClientConfig {

    /**
     * 기본 WebClient Bean
     * - 필요시 주입하여 사용 가능
     */
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
