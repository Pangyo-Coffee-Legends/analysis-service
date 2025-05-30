package com.nhnacademy.workanalysis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * AI 분석 쓰레드 정보를 외부에 전달하기 위한 DTO 클래스입니다.
 * 엔티티를 직접 노출하지 않고 필요한 정보만 전달합니다.
 */
@Value

@AllArgsConstructor
public class AiChatThreadDto {

    /**
     * 쓰레드 고유 식별자입니다.
     */
    @NotNull
    Long threadId;

    /**
     * 해당 쓰레드를 생성한 사원의 고유 번호입니다.
     */
    @NotNull
    Long mbNo;

    /**
     * 쓰레드 제목입니다.
     */
    @NotBlank
    String title;

    /**
     * 쓰레드 생성 일시입니다.
     * ISO 8601 형식으로 직렬화됩니다.
     */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;
}
