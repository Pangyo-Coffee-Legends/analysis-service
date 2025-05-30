package com.nhnacademy.workanalysis.adaptor;

import com.nhnacademy.workanalysis.dto.attendance.AttendanceSummaryDto;
import com.nhnacademy.workanalysis.dto.attendance.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * 최근 30일 출결 요약을 조회할 때 사용 (관리자 대시보드 등)
 * 리포트 생성에는 사용하지 않음.
 */
@FeignClient(name = "work-entry-service", url = "${work.entry.service.url}",path = "/api/v1/attendances")
public interface WorkEntryClient {

    @GetMapping("/{mbNo}/summary/recent")
    PageResponse<AttendanceSummaryDto> getRecent30DaySummary(@PathVariable("mbNo") Long mbNo);

}
