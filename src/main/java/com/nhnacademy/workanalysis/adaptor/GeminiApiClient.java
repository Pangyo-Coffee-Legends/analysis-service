package com.nhnacademy.workanalysis.adaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GeminiApiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gemini API 호출 및 결과 반환
     *
     * @param prompt    프롬프트 내용
     * @param memberNo  사원 번호
     * @return GeminiAnalysisResponse
     */
    public GeminiAnalysisResponse call(String prompt, Long memberNo) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        String requestBody = """
            {
              "contents": [{
                "parts": [{"text": "%s"}]
              }]
            }
            """.formatted(prompt.replace("\"", "\\\""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();

            log.debug("Gemini 응답 원문: {}", responseBody);  // 전체 응답 JSON 출력

            String result = extractText(responseBody);
            log.info("✅ Gemini 응답 성공 - memberNo={} | 결과 요약: {}", memberNo, result.length() > 100 ? result.substring(0, 100) + "..." : result);
            return new GeminiAnalysisResponse(memberNo, result);

        } catch (HttpClientErrorException e) {
            log.error("❌ Gemini API HTTP 오류 - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new GeminiAnalysisResponse(memberNo, "⚠️ Gemini HTTP 오류: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("❌ Gemini API 호출 실패 - message: {}", e.getMessage(), e);
            return new GeminiAnalysisResponse(memberNo, "⚠️ Gemini 예외 발생: " + e.getMessage());
        }
    }

    /**
     * Gemini 응답 JSON에서 텍스트 추출
     *
     * @param json 전체 JSON 문자열
     * @return 추출된 텍스트
     */
    private String extractText(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String text = root.at("/candidates/0/content/parts/0/text").asText(null);
            if (text == null) {
                throw new IllegalStateException("응답 JSON에 분석 결과 텍스트가 없습니다.");
            }
            return text;
        } catch (Exception e) {
            log.warn("⚠️ Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            return "⚠️ 분석 결과 파싱 실패";
        }
    }
}
