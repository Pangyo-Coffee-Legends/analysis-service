package com.nhnacademy.workanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * AI 대화 히스토리를 저장하기 위한 요청 DTO 클래스입니다.
 * 클라이언트가 전달하는 JSON 요청 본문을 매핑하여 서비스에 전달합니다.
 *
 * 필수 입력값:
 * - threadId: 대화가 소속될 쓰레드 ID
 * - role: 메시지를 보낸 주체 (예: "user", "AI")
 * - content: 메시지 본문 텍스트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatHistorySaveRequest {

    /**
     * 대화가 소속된 쓰레드의 고유 ID입니다.
     * 해당 쓰레드가 존재하지 않을 경우 저장에 실패합니다.
     */
    @NotNull
    private Long threadId;

    /**
     * 메시지 작성자의 역할입니다.
     * 예: "user", "AI"
     */
    @NotBlank
    private String role;

    /**
     * 실제 대화 메시지 내용입니다.
     * 공백일 수 없습니다.
     */
    @NotBlank(message = "f메시지 내용은 필수입니다.")
    private String content;
}
