package com.nhnacademy.workanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * 근태 기록 정보를 표현하는 내부 정적 클래스입니다.
 * 프롬프트 생성 시 템플릿으로 사용됩니다.
 */
@Value

@AllArgsConstructor
public class WorkRecordDto {
    /**
     * 근무 일자 (예: "2025-05-19")
     */
    @NotBlank
    String date;

    /**
     * 요일 정보 (예: "월", "화")
     */
    @NotBlank
    String dayOfWeek;

    /**
     * 근태 상태 코드 (예: "1": 출근, "2": 지각 등)
     */
    @NotBlank
    String statusCode;

    /**
     * 출근 시간 (예: "09:00")
     */
    String inTime;

    /**
     * 퇴근 시간 (예: "18:00")
     */
    String outTime;
}
