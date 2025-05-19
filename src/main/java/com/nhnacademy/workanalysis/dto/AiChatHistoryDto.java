package com.nhnacademy.workanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * AI 대화 히스토리 정보를 전달하기 위한 DTO(Data Transfer Object) 클래스입니다.
 * 프론트엔드에 반환될 응답 형태로, 엔티티로부터 필요한 데이터만 추출하여 전송합니다.
 */
@Getter
@AllArgsConstructor
public class AiChatHistoryDto {

    /**
     * 대화 참여자의 역할입니다.
     * 예: "user", "assistant"
     */
    private String role;

    /**
     * 실제 대화 메시지 내용입니다.
     */
    private String content;

    /**
     * 메시지가 생성된 시간입니다.
     * ISO 8601 형식(예: 2025-05-19T10:25:30)으로 프론트에 전달됩니다.
     */
    private LocalDateTime createdAt;
}
