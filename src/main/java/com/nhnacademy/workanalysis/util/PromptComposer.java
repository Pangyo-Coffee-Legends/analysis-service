package com.nhnacademy.workanalysis.util;

import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;

import java.util.*;

public class PromptComposer {

    /**
     * Gemini 멀티턴 메시지 리스트 생성
     *
     * @param request 요청 객체 (프롬프트 + 출결 데이터 포함)
     * @return Gemini API용 message list
     */
    public static List<Map<String, Object>> buildMessages(GeminiAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 ").append(request.getMemberNo()).append("번 사원의 최근 30일 출결 기록입니다.\n");
        for (var record : request.getWorkRecords()) {
            sb.append("- ")
                    .append(record.getDate()).append(" (").append(record.getDayOfWeek()).append(") ")
                    .append("상태: ").append(record.getStatusCode())
                    .append(", 출근: ").append(record.getInTime())
                    .append(", 퇴근: ").append(record.getOutTime()).append("\n");
        }

        return List.of(
                Map.of("role", "user", "parts", List.of(
                        Map.of("text", sb.toString())
                )),
                Map.of("role", "user", "parts", List.of(
                        Map.of("text", request.getMessages())
                ))
        );
    }
}
