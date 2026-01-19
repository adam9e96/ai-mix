package com.aimix_aimixapi.qna.dto.tag;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * QnA 태그 저장 요청 DTO
 * 선택한 태그 목록을 질문에 연결하기 위한 요청
 * questionId는 URL 경로에서 받으므로 Request Body에는 포함하지 않음
 */
@Getter
@Setter
public class QnaTagSaveRequest {

    /**
     * 저장할 태그 이름 목록
     */
    @NotNull(message = "태그 목록은 필수입니다")
    private List<String> tags;

    /**
     * 익명 게시글 비밀번호 (익명 게시글인 경우 필수)
     */
    private String anonymousPassword;
}
