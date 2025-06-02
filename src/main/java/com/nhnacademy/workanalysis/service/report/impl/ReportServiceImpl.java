package com.nhnacademy.workanalysis.service.report.impl;

import com.nhnacademy.workanalysis.adaptor.WorkEntryClient;
import com.nhnacademy.workanalysis.dto.attendance.AttendanceSummaryDto;
import com.nhnacademy.workanalysis.dto.attendance.PageResponse;
import com.nhnacademy.workanalysis.dto.report.AttendanceReportDto;
import com.nhnacademy.workanalysis.exception.WorkEntryRecordNotFoundException;
import com.nhnacademy.workanalysis.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 근태 통계 리포트 생성을 담당하는 서비스 구현 클래스입니다.
 * <p>
 * 사원의 최근 30일 출결 요약 데이터를 기반으로 지정된 연/월에 해당하는 출결 항목을 필터링하고,
 * 근태 코드별 출현 횟수를 집계하여 그래프 및 텍스트 리포트 출력을 위한 데이터로 가공합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final WorkEntryClient workEntryClient;

    /**
     * 사원의 특정 연도/월에 대한 근태 리포트를 생성합니다.
     *
     * @param mbNo  사원 번호
     * @param year  조회할 연도 (예: 2025)
     * @param month 조회할 월 (1~12)
     * @return 근태 상태별 일수와 마크다운 형식 요약을 포함한 DTO
     * @throws WorkEntryRecordNotFoundException 해당 조건에 맞는 근태 데이터가 없을 경우
     */
    @Override
    public AttendanceReportDto generateAttendanceReport(Long mbNo, int year, int month) {
        log.info("📥 근태 리포트 생성 요청 - mbNo={}, year={}, month={}", mbNo, year, month);

        PageResponse<AttendanceSummaryDto> pageResponse = workEntryClient.getRecent30DaySummary(mbNo);

        List<AttendanceSummaryDto> filtered = pageResponse.getContent().stream()
                .filter(r -> r.getYear() == year && r.getMonthValue() == month)
                .toList();

        if (filtered.isEmpty()) {
            log.warn("⚠️ 사원 {}의 {}년 {}월에 대한 출결 기록이 존재하지 않습니다.",mbNo,year,month);
            throw new WorkEntryRecordNotFoundException("출결 데이터 없음");
        }

        log.info("✅ {}건의 출결 기록이 필터링되었습니다.", filtered.size());

        // 근태 코드별 집계
        Map<Long, Long> codeCountMap = filtered.stream()
                .collect(Collectors.groupingBy(AttendanceSummaryDto::getCode, Collectors.counting()));

        log.debug("📊 근태 상태 코드별 집계 결과: {}", codeCountMap);

        // 요약 텍스트 생성
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%d년 %d월 근태 통계 요약%n%n", year, month));
        codeCountMap.forEach((code, count) ->
                summary.append(String.format("- [%s]: %d일%n", mapCodeToLabel(code), count))
        );

        return new AttendanceReportDto(codeCountMap, summary.toString(),year,month);
    }

    /**
     * 근태 상태 코드(Long)를 한글 라벨로 변환합니다.
     *
     * @param code 근태 코드 (1~8)
     * @return 코드에 해당하는 라벨 (예: 출근, 지각, 결근 등)
     */
    private String mapCodeToLabel(Long code) {
        return switch (code.intValue()) {
            case 1 -> "출근";
            case 2 -> "지각";
            case 3 -> "결근";
            case 4 -> "외근";
            case 5 -> "연차";
            case 6 -> "질병";
            case 7 -> "반차";
            case 8 -> "상(喪)";
            default -> {
                log.warn("❓ 알 수 없는 근태 코드: {}", code);
                yield "기타";
            }
        };
    }
}
