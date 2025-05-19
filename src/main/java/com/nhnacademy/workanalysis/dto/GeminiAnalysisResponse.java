package com.nhnacademy.workanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Gemini 분석 요청에 대한 응답 결과를 담는 DTO 클래스입니다.
 */
@Getter
@AllArgsConstructor
public class GeminiAnalysisResponse {

    /**
     * 분석 대상이었던 사원의 고유 번호입니다.
     */
    private Long memberNo;

    /**
     * Gemini API로부터 수신된 전체 응답 텍스트입니다.
     * 마크다운 형식이 포함될 수 있으며, 프론트에서 marked.js로 렌더링됩니다.
     */
    private String fullText;
}
