package com.aimix_aimixapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마이페이지 응답 DTO
 * - 마이페이지에서 사용할 전체 사용자 정보 및 통계
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponse {

    /**
     * 기본 사용자 정보
     */
    private UserResponse userResponse;

    /**
     * 사용자 통계 정보
     */
    private UserStatistics statistics;

    /**
     * 사용자 통계 정보 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatistics {
        /**
         * 배틀 참여 횟수
         */
        private long battleCount;

        /**
         * 채팅 세션 수
         */
        private long chatSessionCount;
    }
}

