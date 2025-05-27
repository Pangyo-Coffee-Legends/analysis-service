package com.nhnacademy.workanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 대화목록(이하 '쓰레드') 제목 수정시 필요한 DTO입니다.
 */
@Data
@NoArgsConstructor
public class AiChatThreadTitleUpdateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
}
