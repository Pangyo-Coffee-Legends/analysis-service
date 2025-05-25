package com.nhnacademy.workanalysis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Gemini API에 전달되는 분석 요청 DTO 클래스입니다.
 * 사원 번호, 멀티턴 대화 메시지, 근태 기록 데이터를 포함합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiAnalysisRequest {

    /**
     * 분석 대상 사원의 고유 번호입니다.
     */
    @NotNull(message = "memberNo는 필수입니다.")
    private Long memberNo;

    /**
     * 사용자가 AI에게 보낸 질문들과 그에 대한 응답들을 포함하는 대화 이력입니다.
     */
    @NotNull(message = "messages는 필수입니다.")
    private List<MessageDto> messages;

    /**
     * 분석에 참고할 근태 기록 리스트입니다.
     * 일자별 출결 상태, 시간 등이 포함됩니다.
     */
    private List<WorkRecordDto> workRecords;


}
