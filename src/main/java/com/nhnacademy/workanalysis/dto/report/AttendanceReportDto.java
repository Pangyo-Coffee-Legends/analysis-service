package com.nhnacademy.workanalysis.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class AttendanceReportDto {
    private Map<Long, Long> statusCountMap;
    private String markdownSummary;


    private int year;
    private int month;
}

