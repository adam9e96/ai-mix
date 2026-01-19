package com.aimix_aimixapi.common.exception.domain.knowledge.contribution;

/**
 * 기여 타입 필수 예외
 * 기여 이력 기록 시 기여 타입이 null인 경우 발생
 */
public class ContributionTypeRequiredException extends RuntimeException {
    public ContributionTypeRequiredException(String message) {
        super(message);
    }
}
