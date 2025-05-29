package com.nhnacademy.workanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 쓰레드 제목 수정 시 필요한 DTO
 */
@Data
@NoArgsConstructor
public class AiChatThreadTitleUpdateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
}
