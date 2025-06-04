package com.nhnacademy.workanalysis.adpator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.workanalysis.adaptor.AiChatApiClient;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.dto.MessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * AiChatApiClient 클래스의 Gemini API 호출 로직에 대한 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class AiChatApiClientTest {

    @InjectMocks
    private AiChatApiClient aiChatApiClient;

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiChatApiClient, "apiKey", "fake-api-key");
        ReflectionTestUtils.setField(aiChatApiClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(aiChatApiClient, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("Gemini API 호출 성공 - 텍스트 정상 반환")
    void testCall_whenSuccess_shouldReturnParsedText() {
        // given
        MessageDto message = new MessageDto("user", "출근 상태를 분석해줘");
        List<MessageDto> messages = List.of(message);

        String apiResponseJson = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "분석 결과입니다."
                      }
                    ]
                  }
                }
              ]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(responseEntity);

        // when
        GeminiAnalysisResponse response = aiChatApiClient.call(messages, 1001L);

        // then
        assertThat(response.getMemberNo()).isEqualTo(1001L);
        assertThat(response.getFullText()).isEqualTo("분석 결과입니다.");
    }

    @Test
    @DisplayName("Gemini API 호출 예외 발생 - 에러 메시지 포함 반환")
    void testCall_whenApiFails_shouldReturnErrorMessage() {
        // given
        MessageDto message = new MessageDto("user", "지각 상태 분석");
        List<MessageDto> messages = List.of(message);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // when
        GeminiAnalysisResponse response = aiChatApiClient.call(messages, 2002L);

        // then
        assertThat(response.getMemberNo()).isEqualTo(2002L);
        assertThat(response.getFullText()).contains("❌ 시스템 오류");
    }

    @Test
    @DisplayName("Gemini 응답 JSON에 텍스트 필드 누락 시 경고 메시지 반환")
    void testCall_whenTextMissing_shouldReturnWarningMessage() {
        // given
        MessageDto message = new MessageDto("user", "결근 상태 분석");
        List<MessageDto> messages = List.of(message);

        String invalidJson = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": []
                  }
                }
              ]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(responseEntity);

        // when
        GeminiAnalysisResponse response = aiChatApiClient.call(messages, 3003L);

        // then
        assertThat(response.getMemberNo()).isEqualTo(3003L);
        assertThat(response.getFullText()).contains("❌ 분석 결과를 찾을 수 없습니다.");
    }
}
