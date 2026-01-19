package com.aimix_aimixapi.qna.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * QnA 태그 생성 응답 DTO
 * GPT로 생성된 추천 태그 목록을 반환
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QnaTagGenerateResponse {

    /**
     * 추천 태그 목록
     */
    private List<String> tags;
}
