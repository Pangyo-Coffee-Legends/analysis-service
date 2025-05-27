package com.nhnacademy.workanalysis.service.report;

import com.nhnacademy.workanalysis.dto.report.AttendanceReportDto;

public interface ReportService {
    /**
     * 사원의 월별 근태 리포트를 생성합니다.
     *
     * @param mbName 사원 번호
     * @param year 연도
     * @param month 월
     * @return 근태 코드별 통계 Map과 요약 텍스트를 포함한 리포트 DTO
     */
    AttendanceReportDto generateAttendanceReport(Long mbName, int year, int month);
}
