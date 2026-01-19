package com.aimix_aimixapi.common.exception.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 * - timestamp: 에러 발생 시각
 * - status   : HTTP 상태 코드
 * - error    : 에러 타입(간단한 식별자)
 * - message  : 상세 메시지
 * - path     : 요청 URI
 * @since 2025-12-15
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
