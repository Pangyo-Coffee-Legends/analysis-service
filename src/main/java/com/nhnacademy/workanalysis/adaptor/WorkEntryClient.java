package com.nhnacademy.workanalysis.adaptor;

import com.nhnacademy.workanalysis.dto.attendance.AttendanceSummaryDto;
import com.nhnacademy.workanalysis.dto.attendance.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 출결 데이터 서비스(work-entry-service)와 통신하기 위한 Feign Client 인터페이스입니다.
 *
 * <p>
 * 이 인터페이스는 관리자 대시보드 등에서 특정 사원의 최근 30일 출결 요약 데이터를 조회할 때 사용됩니다.
 * 주로 근태 분석이나 통계 시각화를 위해 사용되며, PDF 리포트 생성과는 무관합니다.
 * </p>
 *
 * <p>
 * 내부적으로는 {@code /api/v1/attendances/{mbNo}/summary/recent} 엔드포인트를 호출하여,
 * {@link AttendanceSummaryDto} 객체 리스트를 포함하는 페이징 응답 {@link PageResponse} 형태로 반환받습니다.
 * </p>
 */
@FeignClient(name = "work-entry-service", url = "${work.entry.service.url}", path = "/api/v1/attendances")
public interface WorkEntryClient {

    /**
     * 특정 사원의 최근 30일 근무 요약 데이터를 조회합니다.
     *
     * <p>
     * 이 API는 주로 대시보드나 관리자용 통계 페이지에서 사용되며, 페이징된 출결 요약 데이터를 제공합니다.
     * 반환된 데이터는 사원의 출근일, 출근/퇴근 시간, 근무 시간, 출결 상태 등을 포함합니다.
     * </p>
     *
     * @param mbNo 사원의 고유 번호 (예: 101, 203 등)
     * @return {@link AttendanceSummaryDto} 리스트가 포함된 {@link PageResponse} 객체
     */
    @GetMapping("/{mbNo}/summary/recent")
    PageResponse<AttendanceSummaryDto> getRecent30DaySummary(@PathVariable("mbNo") Long mbNo);
}
