package com.aimix_aimixapi.qna.dto.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * GPT 태그 생성 응답 DTO
 * GPT API로부터 받은 태그 목록을 파싱하기 위한 DTO
 */
@Getter
@Setter
public class GptTagResponse {
    
    /**
     * 추천 태그 목록
     */
    @JsonProperty("tags")
    private List<String> tags;
}
