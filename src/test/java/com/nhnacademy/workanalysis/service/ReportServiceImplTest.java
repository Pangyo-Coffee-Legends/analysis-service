package com.nhnacademy.workanalysis.service;

import com.nhnacademy.workanalysis.adaptor.WorkEntryClient;
import com.nhnacademy.workanalysis.dto.attendance.AttendanceSummaryDto;
import com.nhnacademy.workanalysis.dto.attendance.PageResponse;
import com.nhnacademy.workanalysis.dto.report.AttendanceReportDto;
import com.nhnacademy.workanalysis.exception.WorkEntryRecordNotFoundException;
import com.nhnacademy.workanalysis.service.report.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ReportServiceImpl} 클래스의 단위 테스트입니다.
 */
class ReportServiceImplTest {

    private WorkEntryClient workEntryClient;
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        workEntryClient = mock(WorkEntryClient.class);
        reportService = new ReportServiceImpl(workEntryClient);
    }

    /**
     * 정상적인 근태 데이터가 존재할 경우,
     * {@link ReportServiceImpl#generateAttendanceReport(Long, int, int)} 메서드가
     * 정확한 통계 및 요약 내용을 포함한 {@link AttendanceReportDto}를 반환하는지 검증합니다.
     */
    @Test
    @DisplayName("generateAttendanceReport() - 정상 리포트 생성")
    void testGenerateAttendanceReport_success() {
        // given
        Long mbNo = 1L;
        int year = 2025;
        int month = 6;

        List<AttendanceSummaryDto> mockData = List.of(
                new AttendanceSummaryDto(2025, 6, 1, 8, LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0), 1L),
                new AttendanceSummaryDto(2025, 6, 2, 7, LocalDateTime.of(2025, 6, 2, 9, 30), LocalDateTime.of(2025, 6, 2, 18, 0), 2L),
                new AttendanceSummaryDto(2025, 6, 3, 0, null, null, 3L)
        );

        PageResponse<AttendanceSummaryDto> pageResponse = new PageResponse<>(mockData, 1, 1L, 5,0);

        when(workEntryClient.getRecent30DaySummary(mbNo)).thenReturn(pageResponse);

        // when
        AttendanceReportDto report = reportService.generateAttendanceReport(mbNo, year, month);

        // then
        assertThat(report).isNotNull();
        assertThat(report.getStatusCountMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                1L, 1L,
                2L, 1L,
                3L, 1L
        ));
        assertThat(report.getMarkdownSummary()).contains("출근", "지각", "결근");
        assertThat(report.getYear()).isEqualTo(year);
        assertThat(report.getMonth()).isEqualTo(month);

        verify(workEntryClient, times(1)).getRecent30DaySummary(mbNo);
    }

    /**
     * 특정 연도와 월에 해당하는 출결 기록이 없을 경우,
     * {@link WorkEntryRecordNotFoundException} 예외를 발생시키는지 검증합니다.
     */
    @Test
    @DisplayName("generateAttendanceReport() - 출결 데이터 없음 예외 처리")
    void testGenerateAttendanceReport_notFound() {
        // given
        Long mbNo = 2L;
        int year = 2025;
        int month = 6;

        // 5월 데이터만 포함
        List<AttendanceSummaryDto> mockData = List.of(
                new AttendanceSummaryDto(2025, 5, 31, 8, LocalDateTime.of(2025, 5, 31, 9, 0), LocalDateTime.of(2025, 5, 31, 18, 0), 1L)
        );
        PageResponse<AttendanceSummaryDto> pageResponse = new PageResponse<>(mockData, 1, 1L, 5,0);

        when(workEntryClient.getRecent30DaySummary(mbNo)).thenReturn(pageResponse);

        // when & then
        assertThatThrownBy(() -> reportService.generateAttendanceReport(mbNo, year, month))
                .isInstanceOf(WorkEntryRecordNotFoundException.class)
                .hasMessageContaining("출결 데이터 없음");

        verify(workEntryClient, times(1)).getRecent30DaySummary(mbNo);
    }
}
