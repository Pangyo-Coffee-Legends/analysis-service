package com.nhnacademy.workanalysis.dto;

import lombok.*;

/**
 * 대화 메시지를 표현하는 내부 정적 클래스입니다.
 * Gemini의 멀티턴 형식에 맞춰 role과 content 구조를 가집니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    /**
     * 메시지 작성자 역할입니다.
     * 예: "user", "model"
     */
     String role;

    /**
     * 메시지 본문 텍스트입니다.
     */
     String content;
}