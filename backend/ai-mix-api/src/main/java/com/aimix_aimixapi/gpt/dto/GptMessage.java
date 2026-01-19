package com.aimix_aimixapi.gpt.dto;

import lombok.*;

/**
 * GPT 메시지 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GptMessage {
    private GptMessageRole role;
    private String content;
}
