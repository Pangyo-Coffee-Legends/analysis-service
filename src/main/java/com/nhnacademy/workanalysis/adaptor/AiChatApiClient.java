package com.nhnacademy.workanalysis.adaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini API를 호출하는 클라이언트 컴포넌트입니다.
 * 멀티턴 대화 메시지를 기반으로 AI 분석 요청을 수행합니다.
 */
@Slf4j
@Component
public class AiChatApiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gemini 2.0 Flash 모델을 이용하여 분석 요청을 수행합니다.
     * 이전 대화 기록을 포함한 메시지 배열을 전달하며, 응답 결과를 파싱하여 반환합니다.
     *
     * @param messages 이전까지의 대화 이력 + 유저의 현재 질문
     * @param memberNo 분석 대상 사원 번호
     * @return GeminiAnalysisResponse 분석 응답 결과 (성공 또는 실패 메시지 포함)
     */
    public GeminiAnalysisResponse call(List<GeminiAnalysisRequest.Message> messages, Long memberNo) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        // 요청 본문 구성: role + parts(text)
        List<Map<String, Object>> partsList = messages.stream()
                .map(m -> Map.of("role", m.getRole(), "parts", List.of(Map.of("text", m.getContent()))))
                .toList();

        Map<String, Object> body = Map.of("contents", partsList);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.debug("🛰️ Gemini API 요청 준비 완료 - memberNo: {}, 메시지 수: {}", memberNo, messages.size());
        log.trace("Gemini 요청 본문: {}", body);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("✅ Gemini API 응답 수신 - HTTP 상태: {}", response.getStatusCode());
            String responseBody = response.getBody();
            log.debug("🔎 Gemini 응답 원문: {}", responseBody);
            String text = extractText(responseBody);
            return new GeminiAnalysisResponse(memberNo, text);
        } catch (Exception e) {
            log.error("❌ Gemini API 호출 실패 - memberNo: {}, 에러: {}", memberNo, e.getMessage(), e);
            return new GeminiAnalysisResponse(memberNo, "⚠️ Gemini 호출 실패: " + e.getMessage());
        }
    }

    /**
     * Gemini 응답 JSON에서 분석 결과 텍스트를 추출합니다.
     * 예상 경로: /candidates/0/content/parts/0/text
     *
     * @param json Gemini API의 원시 JSON 응답 문자열
     * @return 분석된 텍스트 결과
     */
    private String extractText(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String text = root.at("/candidates/0/content/parts/0/text").asText(null);
            if (text == null) {
                log.warn("⚠️ 분석 결과 누락 - 응답에 텍스트 필드가 존재하지 않음");
                throw new IllegalStateException("응답 JSON에 분석 결과 텍스트가 없습니다.");
            }
            log.debug("📦 Gemini 응답 텍스트 추출 성공");
            return text;
        } catch (Exception e) {
            log.warn("⚠️ Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            return "⚠️ 분석 결과 파싱 실패";
        }
    }
}
