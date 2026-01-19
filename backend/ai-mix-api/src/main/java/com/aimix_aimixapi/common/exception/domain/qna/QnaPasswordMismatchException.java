package com.aimix_aimixapi.common.exception.domain.qna;

/**
 * QNA 비밀번호 불일치 예외
 * - 익명 게시글 비밀번호가 일치하지 않을 때 발생
 */
public class QnaPasswordMismatchException extends RuntimeException {
    public QnaPasswordMismatchException(String message) {
        super(message);
    }
}
