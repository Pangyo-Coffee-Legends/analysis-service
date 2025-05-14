package com.nhnacademy.workanalysis.util;

import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;

public class PromptComposer {

    public static String compose(GeminiAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("[사원번호: ").append(request.getMemberNo()).append("]에 대한 근무 기록:\n");

        for (var record : request.getWorkRecords()) {
            sb.append("- ").append(record.getDate())
                    .append(" (").append(record.getDayOfWeek()).append(") ")
                    .append("상태: ").append(record.getStatusCode())
                    .append(", 출근: ").append(record.getInTime())
                    .append(", 퇴근: ").append(record.getOutTime()).append("\n");
        }

        sb.append("\n[사용자 프롬프트]:\n").append(request.getPrompt());

        return sb.toString();
    }
}
