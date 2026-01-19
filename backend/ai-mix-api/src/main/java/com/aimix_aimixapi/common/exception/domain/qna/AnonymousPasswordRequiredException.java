package com.aimix_aimixapi.common.exception.domain.qna;

/**
 * 익명 게시글 비밀번호 필수 예외
 * - 익명 게시글 작성/수정 시 비밀번호가 제공되지 않은 경우
 */
public class AnonymousPasswordRequiredException extends RuntimeException {
    public AnonymousPasswordRequiredException(String message) {
        super(message);
    }
}
