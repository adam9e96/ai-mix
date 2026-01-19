package com.aimix_aimixapi.gpt.token.validator;

import lombok.extern.log4j.Log4j2;

/**
 * GPT 토큰 사용량 유효성 검증 유틸리티
 * API 파라미터 유효성 검증 및 기본값 설정을 담당하는 유틸리티 클래스
 */
@Log4j2
public class GptTokenUsageValidator {

    /**
     * period 파라미터 유효성 검증 및 기본값 반환
     * <p>유효한 period 값은 "daily", "weekly", "monthly"입니다.
     * 유효하지 않은 경우 기본값 "daily"를 반환하고 경고 로그를 남깁니다.
     *
     * @param period 검증할 period 값
     * @return 유효한 period 값 (기본값: "daily")
     */
    public static String validatePeriod(String period) {
        if (isValidPeriod(period)) {
            return period;
        }

        log.warn("잘못된 period 값: {}, 기본값 daily 사용", period);
        return "daily";
    }

    /**
     * period 값이 유효한지 확인
     *
     * @param period 확인할 period 값
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValidPeriod(String period) {
        return period != null && (period.equals("daily") || period.equals("weekly") || period.equals("monthly"));
    }

    /**
     * days 파라미터 유효성 검증 및 기본값 반환
     * <p>유효한 days 값은 1 이상 365 이하입니다.
     * 유효하지 않은 경우 기본값 30을 반환하고 경고 로그를 남깁니다.
     *
     * @param days 검증할 days 값
     * @return 유효한 days 값 (기본값: 30)
     */
    public static int validateDays(int days) {
        if (isValidDays(days)) {
            return days;
        }

        log.warn("잘못된 days 값: {}, 기본값 30 사용", days);
        return 30;
    }

    /**
     * days 값이 유효한지 확인
     *
     * @param days 확인할 days 값
     * @return 유효하면 true (1~365), 그렇지 않으면 false
     */
    public static boolean isValidDays(int days) {
        return days >= 1 && days <= 365;
    }
}
