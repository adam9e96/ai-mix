package com.aimix_aimixapi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 중복 체크 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckResponse {

    private boolean available;
    private String message;
}

