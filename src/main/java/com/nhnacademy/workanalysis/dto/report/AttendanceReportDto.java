package com.nhnacademy.workanalysis.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 근태 리포트 생성을 위한 응답 DTO입니다.
 *
 * <p>근태 상태별 일수 통계, 마크다운 형식의 요약, 연도 및 월 정보를 포함합니다.</p>
 */
@Data
@AllArgsConstructor
public class AttendanceReportDto {

    /**
     * 근태 상태별 일수 통계를 담은 맵
     * <p>예: key = 근태 코드(1~8), value = 해당 코드의 일수</p>
     */
    private Map<Long, Long> statusCountMap;

    /**
     * 근태 상태에 대한 분석 결과를 마크다운 형식으로 요약한 문자열
     */
    private String markdownSummary;

    /**
     * 리포트 대상 연도
     */
    private int year;

    /**
     * 리포트 대상 월
     */
    private int month;
}
