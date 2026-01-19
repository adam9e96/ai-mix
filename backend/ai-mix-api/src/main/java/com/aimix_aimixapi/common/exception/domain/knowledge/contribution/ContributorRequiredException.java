package com.aimix_aimixapi.common.exception.domain.knowledge.contribution;

/**
 * 기여자 필수 예외
 * 기여 이력 기록 시 기여자가 null인 경우 발생
 */
public class ContributorRequiredException extends RuntimeException {
    public ContributorRequiredException(String message) {
        super(message);
    }
}
