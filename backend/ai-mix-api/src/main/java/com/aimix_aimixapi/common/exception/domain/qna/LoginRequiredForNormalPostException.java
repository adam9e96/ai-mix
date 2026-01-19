package com.aimix_aimixapi.common.exception.domain.qna;

/**
 * 일반 게시글 작성 로그인 필수 예외
 * - 일반 게시글 작성 시 로그인이 필요한 경우
 */
public class LoginRequiredForNormalPostException extends RuntimeException {
    public LoginRequiredForNormalPostException(String message) {
        super(message);
    }
}
